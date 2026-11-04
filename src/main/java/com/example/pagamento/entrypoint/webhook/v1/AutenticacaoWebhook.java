package com.example.pagamento.entrypoint.webhook.v1;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Autenticação dos webhooks: preocupação da borda, não do domínio. Comparação em tempo constante. */
final class AutenticacaoWebhook {

    static final String HEADER = "X-Webhook-Token";

    private AutenticacaoWebhook() {
    }

    static void exigir(String recebido, String esperado) {
        var valido = recebido != null && MessageDigest.isEqual(
                recebido.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8));
        if (!valido) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }
}
