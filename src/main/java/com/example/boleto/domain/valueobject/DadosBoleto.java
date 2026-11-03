package com.example.boleto.domain.valueobject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Os dados que definem um boleto, agrupados num value object.
 * Valida só o FORMATO (via VOs). As regras de NEGÓCIO ficam nas specifications.
 */
public record DadosBoleto(Pagador pagador, Dinheiro valor, LocalDate dataVencimento) {

    public DadosBoleto {
        Objects.requireNonNull(pagador, "pagador é obrigatório");
        Objects.requireNonNull(valor, "valor é obrigatório");
        Objects.requireNonNull(dataVencimento, "data de vencimento é obrigatória");
    }

    public static DadosBoleto de(Pagador pagador, BigDecimal valor, LocalDate dataVencimento) {
        return new DadosBoleto(pagador, new Dinheiro(valor), dataVencimento);
    }

    /** Novo valor/vencimento, mesmo pagador (o pagador de um boleto não muda). */
    public DadosBoleto comValorEVencimento(BigDecimal novoValor, LocalDate novoVencimento) {
        return de(pagador, novoValor, novoVencimento);
    }
}
