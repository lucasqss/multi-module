package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo;

import java.math.BigDecimal;

/** Payloads que o pagamento deixa na saída assíncrona. Os adaptadores de integração os leem. */
public final class MensagensPagamento {

    public static final String ENVIAR_ORDEM = "ENVIAR_ORDEM";
    public static final String INFORMAR_UTILIZACAO = "INFORMAR_UTILIZACAO";

    private MensagensPagamento() {
    }

    public record Destinatario(String cpfCnpj, String nome, String banco, String agencia, String conta, String chavePix) {
    }

    public record EnviarOrdem(Long idOrdem, BigDecimal valor, String meio, Destinatario destinatario) {
    }

    public record InformarUtilizacao(String cotaRef, BigDecimal valor, String referencia) {
    }
}
