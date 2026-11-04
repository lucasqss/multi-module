package com.example.pagamento.adapter.banco;

import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.valueobject.Dinheiro;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Integração de SAÍDA com o BANCO: envia o pagamento (os boletos, debitando a conta informada).
 * O resultado volta depois pela ENTRADA {@code entrypoint.webhook.v1.BancoWebhookResource}.
 *
 * <p>SIMULAÇÃO: na vida real seria um REST client/arquivo de remessa. Aqui só registra o envio.
 */
@ApplicationScoped
public class BancoPagamentos {

    private static final Logger LOG = Logger.getLogger(BancoPagamentos.class);

    private final List<EnvioRegistrado> envios = new CopyOnWriteArrayList<>();

    public void enviar(Pagamento pagamento) {
        var idTransacao = pagamento.getIdTransacao()
                .orElseThrow(() -> new IllegalStateException("Pagamento sem transação: " + pagamento.getId()));
        envios.add(new EnvioRegistrado(idTransacao, pagamento.getValorTotal(), pagamento.getBoletos().quantidade()));
        LOG.infof("[SIMULAÇÃO] Banco: transação %s, %d boleto(s), total %s",
                idTransacao, pagamento.getBoletos().quantidade(), pagamento.getValorTotal());
    }

    public List<EnvioRegistrado> envios() {
        return List.copyOf(envios);
    }

    public record EnvioRegistrado(String idTransacao, Dinheiro valorTotal, int quantidadeBoletos) {
    }
}
