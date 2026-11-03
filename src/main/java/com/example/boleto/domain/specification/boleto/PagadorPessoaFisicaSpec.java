package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.specification.Specification;

/** Bloco de construção: "o pagador é pessoa física (CPF)". */
public class PagadorPessoaFisicaSpec implements Specification<SolicitacaoBoleto> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return s.dados().pagador().documento().pessoaFisica();
    }
}
