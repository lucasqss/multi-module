package com.example.financiamento.adapter.registro;

import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Integração de SAÍDA com o ÓRGÃO DE REGISTRO da garantia: cartório de imóveis (IMOVEL) ou órgão de
 * trânsito/gravame (VEICULO). O resultado volta depois pela ENTRADA {@code entrypoint.webhook.v1.RegistroWebhookResource}.
 *
 * <p>SIMULAÇÃO: na vida real seriam dois clientes REST; aqui só registra o envio.
 */
@ApplicationScoped
public class RegistroDeGarantias {

    private static final Logger LOG = Logger.getLogger(RegistroDeGarantias.class);

    private final List<EnvioRegistrado> envios = new CopyOnWriteArrayList<>();

    public void enviar(Financiamento financiamento) {
        var protocolo = financiamento.getIdProtocolo()
                .orElseThrow(() -> new IllegalStateException("Financiamento sem protocolo: " + financiamento.getId()))
                .toString();
        var bem = financiamento.getBem().orElseThrow().identificacao();
        envios.add(new EnvioRegistrado(protocolo, financiamento.getTipo(), bem));
        LOG.infof("[SIMULAÇÃO] Registro: protocolo %s, bem %s, enviado ao %s",
                protocolo, bem, financiamento.getTipo().orgaoDeRegistro());
    }

    public List<EnvioRegistrado> envios() {
        return List.copyOf(envios);
    }

    public record EnvioRegistrado(String idProtocolo, TipoFinanciamento tipo, String identificacaoDoBem) {
    }
}
