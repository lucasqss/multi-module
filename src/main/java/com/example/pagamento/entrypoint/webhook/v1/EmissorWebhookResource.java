package com.example.pagamento.entrypoint.webhook.v1;

import com.example.pagamento.application.processo.ReceberCancelamentoBoletoUC;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * ENTRADA do SISTEMA EMISSOR: o ciclo de vida do boleto é dele; ele nos avisa quando cancela um boleto.
 * Responde 200 mesmo que nenhum processo nosso tenha o boleto (o aviso foi recebido).
 */
@Path("/webhooks/v1/emissor/boletos-cancelados")
public class EmissorWebhookResource {

    private final ReceberCancelamentoBoletoUC receberCancelamento;
    private final String token;

    public EmissorWebhookResource(ReceberCancelamentoBoletoUC receberCancelamento,
                                  @ConfigProperty(name = "emissor.webhook.token") String token) {
        this.receberCancelamento = receberCancelamento;
        this.token = token;
    }

    @POST
    public CancelamentoResponse receber(@HeaderParam(AutenticacaoWebhook.HEADER) String tokenRecebido,
                                        @Valid @NotNull BoletoCanceladoRequest aviso) {
        AutenticacaoWebhook.exigir(tokenRecebido, token);
        return new CancelamentoResponse(receberCancelamento.executar(aviso.idBoleto()));
    }

    public record CancelamentoResponse(int processosAfetados) {
    }
}
