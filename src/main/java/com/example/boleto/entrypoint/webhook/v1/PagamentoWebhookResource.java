package com.example.boleto.entrypoint.webhook.v1;

import com.example.boleto.application.boleto.ReceberPagamentoBoletoUC;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Entrypoint de ENTRADA de integração: o sistema externo de pagamentos avisa que um boleto foi pago.
 * Fica separado de {@code api.v1} porque o cliente é outro sistema, com contrato e autenticação próprios;
 * mesmo assim, só traduz e chama um use case, como qualquer entrypoint.
 *
 * <p>Respostas para o sistema externo: 200 = processado (inclusive aviso repetido); 401 = token inválido;
 * 404 = linha digitável desconhecida; 409 = boleto já quitado por outra transação (conciliação manual).
 */
@Path("/webhooks/v1/pagamentos")
public class PagamentoWebhookResource {

    private final ReceberPagamentoBoletoUC receberPagamento;
    private final String tokenEsperado;

    public PagamentoWebhookResource(ReceberPagamentoBoletoUC receberPagamento,
                                    @ConfigProperty(name = "pagamentos.webhook.token") String tokenEsperado) {
        this.receberPagamento = receberPagamento;
        this.tokenEsperado = tokenEsperado;
    }

    @POST
    public AvisoPagamentoResponse receber(@HeaderParam("X-Webhook-Token") String token,
                                          @Valid @NotNull AvisoPagamentoRequest aviso) {
        autenticar(token);
        return AvisoPagamentoResponse.de(receberPagamento.executar(aviso.paraInput()));
    }

    /** Autenticação é preocupação da borda, não do domínio. Comparação em tempo constante. */
    private void autenticar(String token) {
        var valido = token != null && MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8), tokenEsperado.getBytes(StandardCharsets.UTF_8));
        if (!valido) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }
}
