package com.example.pagamento.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * O boleto NÃO é nosso: o ciclo de vida dele (emissão, alteração, cancelamento) pertence ao sistema
 * emissor. Aqui guardamos uma CÓPIA dos dados que valem para este pagamento (para regras e auditoria),
 * obtida do emissor pelo adapter. Mudanças posteriores no emissor chegam como fato (webhook).
 */
@Embeddable
public record BoletoAPagar(@Column(name = "id_no_emissor", nullable = false, length = 40) String idNoEmissor,
                           @Column(name = "linha_digitavel", nullable = false, length = 47) LinhaDigitavel linhaDigitavel,
                           @Column(nullable = false, precision = 15, scale = 2) Dinheiro valor,
                           @Column(nullable = false) LocalDate vencimento,
                           @Column(nullable = false) String beneficiario) {

    public BoletoAPagar {
        Objects.requireNonNull(idNoEmissor);
        Objects.requireNonNull(linhaDigitavel);
        Objects.requireNonNull(valor);
        Objects.requireNonNull(vencimento);
        Objects.requireNonNull(beneficiario);
    }
}
