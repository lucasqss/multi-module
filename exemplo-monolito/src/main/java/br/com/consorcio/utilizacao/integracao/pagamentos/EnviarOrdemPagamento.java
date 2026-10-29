package br.com.consorcio.utilizacao.integracao.pagamentos;

import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento.EnviarOrdem;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.OrdemRepository;
import br.com.consorcio.utilizacao.integracao.outbox.Despachante;
import br.com.consorcio.utilizacao.integracao.outbox.MensagemOutbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** Adaptador de saída: conhece o modelo do pagamento (repositório e payload), nunca o contrário. */
@ApplicationScoped
public class EnviarOrdemPagamento implements Despachante {

    @Inject
    @RestClient
    PagamentosClient client;

    @Inject
    OrdemRepository ordens;

    @Inject
    ObjectMapper json;

    @Override
    public String tipo() {
        return MensagensPagamento.ENVIAR_ORDEM;
    }

    @Override
    public void enviar(MensagemOutbox mensagem) throws Exception {
        EnviarOrdem ordem = json.readValue(mensagem.payload(), EnviarOrdem.class);
        PagamentosClient.Aceite aceite = client.enviar(mensagem.chaveIdempotencia(), ordem);
        ordens.findById(ordem.idOrdem()).marcarEnviada(aceite.idExterno());
    }
}
