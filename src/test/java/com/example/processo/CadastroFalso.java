package com.example.processo;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Faz o papel do sistema externo de cadastro nos ITs. O jar roda no perfil prod, sem a simulação do dev, então a
 * chamada é HTTP de verdade pelo REST Client: GET /situacoes/{documento} responde SUSPENSA para
 * {@link #CPF_IRREGULAR}, erro 500 para {@link #CPF_SISTEMA_FORA} e REGULAR para os demais.
 */
public class CadastroFalso implements QuarkusTestResourceLifecycleManager {

    public static final String CPF_IRREGULAR = "11144477735";
    public static final String CPF_SISTEMA_FORA = "39053344705";

    private HttpServer servidor;

    @Override
    public Map<String, String> start() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/situacoes/", troca -> {
            var documento = troca.getRequestURI().getPath().substring("/situacoes/".length());
            if (documento.equals(CPF_SISTEMA_FORA)) {
                troca.sendResponseHeaders(500, -1);
                troca.close();
                return;
            }
            var situacao = documento.equals(CPF_IRREGULAR) ? "SUSPENSA" : "REGULAR";
            var corpo = "{\"documento\":\"%s\",\"situacao\":\"%s\"}".formatted(documento, situacao)
                    .getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(200, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        servidor.start();
        return Map.of("quarkus.rest-client.cadastro.url", "http://localhost:" + servidor.getAddress().getPort());
    }

    @Override
    public void stop() {
        servidor.stop(0);
    }
}
