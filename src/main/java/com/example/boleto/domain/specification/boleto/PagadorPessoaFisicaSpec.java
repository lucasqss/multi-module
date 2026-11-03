package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.specification.Specification;

/** Bloco de construção: "o pagador é pessoa física (CPF)". */
public class PagadorPessoaFisicaSpec implements Specification<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return s.pagador().documento().pessoaFisica();
    }
}
