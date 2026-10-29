package br.com.consorcio.utilizacao.integracao.core;

import br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo.ConsultaCore;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo.CotaCore;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Lê a view estável que o time do core publica. Nunca lê tabelas internas do core. */
@ApplicationScoped
public class ConsultaCoreJdbc implements ConsultaCore {

    private static final String SQL = """
            SELECT cota_ref, categoria, regime_normativo, cpf_titular, contemplada, saldo_utilizavel
              FROM core.v_cota_credito
             WHERE cota_ref = ?""";

    @Inject
    @DataSource("core")
    AgroalDataSource core;

    @Override
    public Optional<CotaCore> cota(String cotaRef) {
        try (Connection c = core.getConnection(); PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setString(1, cotaRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new CotaCore(rs.getString("cota_ref"), rs.getString("categoria"),
                        rs.getString("regime_normativo"), rs.getString("cpf_titular"),
                        "S".equals(rs.getString("contemplada")), rs.getBigDecimal("saldo_utilizavel")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao consultar o core", e);
        }
    }
}
