package br.com.consorcio.utilizacao.integracao.pagamentos;

import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento.EnviarOrdem;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pagamentos")
@Path("/ordens")
public interface PagamentosClient {

    record Aceite(String idExterno) {
    }

    @POST
    Aceite enviar(@HeaderParam("Idempotency-Key") String chave, EnviarOrdem ordem);
}
