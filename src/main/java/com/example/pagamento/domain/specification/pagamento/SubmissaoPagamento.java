package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Modalidade;

import java.time.LocalDate;
import java.util.Optional;

/**
 * CANDIDATO das regras de SUBMISSÃO (completude): o pagamento como está ao fim da edição + "hoje".
 * Conta e data podem faltar (o rascunho é incompleto); as regras de completude dizem o que falta.
 */
public record SubmissaoPagamento(Modalidade modalidade, BoletosDoPagamento boletos, ContaDebito contaDebito,
                                 LocalDate dataPagamento, LocalDate hoje) {

    public Optional<LocalDate> data() {
        return Optional.ofNullable(dataPagamento);
    }
}
