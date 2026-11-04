package com.example.financiamento.entrypoint.webhook.v1;

import com.example.financiamento.application.financiamento.ReceberRetornoRegistroUC;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * ENTRADA do ÓRGÃO DE REGISTRO (cartório ou trânsito): resultado do registro da garantia. Separado de
 * {@code api.v1} porque o cliente é outro sistema, com contrato e autenticação próprios; mesmo assim, só
 * traduz e chama um use case.
 *
 * <p>Respostas: 200 = processado (inclusive retorno repetido ou de envio antigo); 400 = protocolo mal formado;
 * 401 = token inválido; 404 = financiamento inexistente; 409 = não espera retorno (ex.: cancelado) ou bloqueado.
 */
@Path("/webhooks/v1/registro/retornos")
public class RegistroWebhookResource {

    private final ReceberRetornoRegistroUC receberRetorno;
    private final String token;

    public RegistroWebhookResource(ReceberRetornoRegistroUC receberRetorno,
                                   @ConfigProperty(name = "registro.webhook.token") String token) {
        this.receberRetorno = receberRetorno;
        this.token = token;
    }

    @POST
    public WebhookResponse receber(@HeaderParam(AutenticacaoWebhook.HEADER) String tokenRecebido,
                                   @Valid @NotNull RetornoRegistroRequest retorno) {
        AutenticacaoWebhook.exigir(tokenRecebido, token);
        var financiamento = receberRetorno.executar(retorno.paraInput());
        return new WebhookResponse(financiamento.id().toString(), financiamento.status().name());
    }

    /** Resposta mínima: o sistema externo só precisa saber que o aviso foi processado. */
    public record WebhookResponse(String financiamentoId, String status) {
    }
}
