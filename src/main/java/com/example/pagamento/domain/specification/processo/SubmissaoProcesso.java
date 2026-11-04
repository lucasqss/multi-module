package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.ContaDebito;

import java.time.LocalDate;
import java.util.Optional;

/**
 * CANDIDATO das regras de SUBMISSÃO (completude): o processo como está ao fim da edição + "hoje".
 * Conta e data podem faltar (o rascunho é incompleto); as regras de completude dizem o que falta.
 */
public record SubmissaoProcesso(BoletosDoProcesso boletos, ContaDebito contaDebito, LocalDate dataPagamento,
                                LocalDate hoje) {

    public Optional<LocalDate> data() {
        return Optional.ofNullable(dataPagamento);
    }
}
