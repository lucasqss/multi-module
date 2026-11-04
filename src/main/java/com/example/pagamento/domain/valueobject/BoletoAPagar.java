package com.example.pagamento.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

/**
 * O boleto NÃO é nosso: o ciclo de vida dele (emissão, alteração, cancelamento) pertence ao sistema
 * emissor. Aqui guardamos uma CÓPIA dos dados que valem para este pagamento (para regras e auditoria),
 * obtida do emissor pelo adapter. Mudanças posteriores no emissor chegam como fato (webhook).
 */
public record BoletoAPagar(String idNoEmissor, LinhaDigitavel linhaDigitavel, Dinheiro valor,
                           LocalDate vencimento, String beneficiario) {

    public BoletoAPagar {
        Objects.requireNonNull(idNoEmissor);
        Objects.requireNonNull(linhaDigitavel);
        Objects.requireNonNull(valor);
        Objects.requireNonNull(vencimento);
        Objects.requireNonNull(beneficiario);
    }
}
