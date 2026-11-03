package com.example.consorcio.domain.valueobject;

import com.example.consorcio.domain.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Os dados "editáveis" de um processo de pagamento, agrupados num value object.
 * Valida só o FORMATO (via VOs). As regras de NEGÓCIO ficam nas specifications.
 */
public record DadosPagamento(Dinheiro valor, FormaPagamento formaPagamento,
                             LocalDate dataVencimento, ContaDebito contaDebito) {

    public DadosPagamento {
        Objects.requireNonNull(valor, "valor é obrigatório");
        Objects.requireNonNull(formaPagamento, "forma de pagamento é obrigatória");
        Objects.requireNonNull(dataVencimento, "data de vencimento é obrigatória");
    }

    /** Monta a partir de tipos primitivos (vindos da application). Conta é opcional. */
    public static DadosPagamento de(BigDecimal valor, FormaPagamento formaPagamento, LocalDate dataVencimento,
                                    String agenciaDebito, String contaDebito) {
        var conta = agenciaDebito == null && contaDebito == null ? null : new ContaDebito(agenciaDebito, contaDebito);
        return new DadosPagamento(new Dinheiro(valor), formaPagamento, dataVencimento, conta);
    }

    public Optional<ContaDebito> conta() {
        return Optional.ofNullable(contaDebito);
    }
}
