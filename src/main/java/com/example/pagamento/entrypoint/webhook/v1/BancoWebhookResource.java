package com.example.pagamento.entrypoint.webhook.v1;

import com.example.pagamento.application.pagamento.ReceberRetornoBancoUC;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * ENTRADA do BANCO: resultado de um envio. Separado de {@code api.v1} porque o cliente é outro sistema,
 * com contrato e autenticação próprios; mesmo assim, só traduz e chama um use case.
 *
 * <p>Respostas: 200 = processado (inclusive retorno repetido); 401 = token inválido;
 * 404 = transação desconhecida; 409 = o pagamento não espera retorno (ex.: cancelado) ou está bloqueado.
 */
@Path("/webhooks/v1/banco/retornos")
public class BancoWebhookResource {

    private final ReceberRetornoBancoUC receberRetorno;
    private final String token;

    public BancoWebhookResource(ReceberRetornoBancoUC receberRetorno,
                                @ConfigProperty(name = "banco.webhook.token") String token) {
        this.receberRetorno = receberRetorno;
        this.token = token;
    }

    @POST
    public WebhookResponse receber(@HeaderParam(AutenticacaoWebhook.HEADER) String tokenRecebido,
                                   @Valid @NotNull RetornoBancoRequest retorno) {
        AutenticacaoWebhook.exigir(tokenRecebido, token);
        var pagamento = receberRetorno.executar(retorno.paraInput());
        return new WebhookResponse(pagamento.id().toString(), pagamento.status().name());
    }

    /** Resposta mínima: o sistema externo só precisa saber que o aviso foi processado. */
    public record WebhookResponse(String pagamentoId, String status) {
    }
}
