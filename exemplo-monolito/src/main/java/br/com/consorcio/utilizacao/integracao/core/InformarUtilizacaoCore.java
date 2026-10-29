package br.com.consorcio.utilizacao.integracao.core;

import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento.InformarUtilizacao;
import br.com.consorcio.utilizacao.integracao.outbox.Despachante;
import br.com.consorcio.utilizacao.integracao.outbox.MensagemOutbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.CallableStatement;
import java.sql.Connection;

/**
 * Escrita no core só por procedure. O datasource "core" não participa da nossa transação
 * (jdbc.transactions=disabled): a procedure confirma sozinha, e a referência a torna idempotente.
 */
@ApplicationScoped
public class InformarUtilizacaoCore implements Despachante {

    @Inject
    @DataSource("core")
    AgroalDataSource core;

    @Inject
    ObjectMapper json;

    @Override
    public String tipo() {
        return MensagensPagamento.INFORMAR_UTILIZACAO;
    }

    @Override
    public void enviar(MensagemOutbox mensagem) throws Exception {
        InformarUtilizacao u = json.readValue(mensagem.payload(), InformarUtilizacao.class);
        try (Connection c = core.getConnection();
             CallableStatement cs = c.prepareCall("{call core.prc_informar_utilizacao(?, ?, ?)}")) {
            cs.setString(1, u.cotaRef());
            cs.setBigDecimal(2, u.valor());
            cs.setString(3, u.referencia());
            cs.execute();
            if (!c.getAutoCommit()) {
                c.commit();
            }
        }
    }
}
