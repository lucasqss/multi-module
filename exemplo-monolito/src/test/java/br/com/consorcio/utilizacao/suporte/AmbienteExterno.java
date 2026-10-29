package br.com.consorcio.utilizacao.suporte;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.oracle.OracleContainer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Sobe tudo o que a aplicação não controla:
 * <ul>
 *   <li>um Oracle (Testcontainers) com três usuários: APP (nosso schema, Flyway), CORE (dono do core,
 *       simulado) e CORE_INTEGRACAO (o usuário com que a aplicação lê views e chama a procedure);</li>
 *   <li>um WireMock com os stubs do sistema de pagamentos e do GED.</li>
 * </ul>
 * Na produção, CORE fica em outro banco ou schema; aqui fica no mesmo container por economia.
 */
public class AmbienteExterno implements QuarkusTestResourceLifecycleManager {

    static final String SENHA = "Senha_123";

    private static OracleContainer oracle;
    private static WireMockServer wiremock;

    @Override
    public Map<String, String> start() {
        oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
                .withUsername("app")
                .withPassword(SENHA);
        oracle.start();
        prepararCore();

        wiremock = new WireMockServer(wireMockConfig().dynamicPort());
        wiremock.start();
        stubsPadrao();

        return Map.of(
                "quarkus.datasource.jdbc.url", oracle.getJdbcUrl(),
                "quarkus.datasource.username", "app",
                "quarkus.datasource.password", SENHA,
                "quarkus.datasource.\"core\".jdbc.url", oracle.getJdbcUrl(),
                "quarkus.datasource.\"core\".username", "core_integracao",
                "quarkus.datasource.\"core\".password", SENHA,
                "quarkus.rest-client.pagamentos.url", wiremock.baseUrl() + "/pagamentos",
                "quarkus.rest-client.ged.url", wiremock.baseUrl() + "/ged");
    }

    @Override
    public void stop() {
        if (wiremock != null) {
            wiremock.stop();
        }
        if (oracle != null) {
            oracle.stop();
        }
    }

    public static WireMockServer wiremock() {
        return wiremock;
    }

    /** Conexão direta ao core, para o teste conferir o que a procedure gravou. */
    public static Connection conexaoCore() throws SQLException {
        return DriverManager.getConnection(oracle.getJdbcUrl(), "core", SENHA);
    }

    public static void stubsPadrao() {
        wiremock.resetAll();
        wiremock.stubFor(post(urlEqualTo("/ged/documentos"))
                .willReturn(okJson("{\"id\":\"GED-0001\"}")));
        wiremock.stubFor(post(urlEqualTo("/pagamentos/ordens"))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")
                        .withBody("{\"idExterno\":\"PAG-0001\"}")));
    }

    private static void prepararCore() {
        try (Connection sys = DriverManager.getConnection(oracle.getJdbcUrl(), "system", SENHA);
             Statement st = sys.createStatement()) {
            for (String usuario : new String[]{"core", "core_integracao"}) {
                st.execute("CREATE USER " + usuario + " IDENTIFIED BY " + SENHA + " QUOTA UNLIMITED ON users");
                st.execute("GRANT CREATE SESSION TO " + usuario);
            }
            st.execute("GRANT CREATE TABLE, CREATE VIEW, CREATE PROCEDURE, CREATE SEQUENCE TO core");
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao criar usuários do core", e);
        }
        try (Connection core = DriverManager.getConnection(oracle.getJdbcUrl(), "core", SENHA);
             Statement st = core.createStatement()) {
            for (String comando : script("/core/core-schema.sql").split("\\n/\\s*\\n")) {
                String sql = comando.lines().filter(l -> !l.strip().startsWith("--"))
                        .collect(java.util.stream.Collectors.joining("\n")).strip();
                if (!sql.isEmpty()) {
                    st.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao criar o schema do core", e);
        }
    }

    private static String script(String recurso) {
        try (InputStream in = AmbienteExterno.class.getResourceAsStream(recurso)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8) + "\n";
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
