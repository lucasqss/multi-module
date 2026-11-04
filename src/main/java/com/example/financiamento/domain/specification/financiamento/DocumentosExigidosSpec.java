package com.example.financiamento.domain.specification.financiamento;

import com.example.financiamento.domain.exception.DocumentoObrigatorioAusenteException;
import com.example.financiamento.domain.specification.RegraNegocio;
import com.example.financiamento.domain.valueobject.TipoDocumento;

import java.util.List;

/** Exige um conjunto de documentos. Parametrizada: um conjunto para todos, outro por tipo. Mensagem diz qual falta. */
public class DocumentosExigidosSpec extends RegraNegocio<SubmissaoFinanciamento> {

    private final List<TipoDocumento> exigidos;

    public DocumentosExigidosSpec(TipoDocumento... exigidos) {
        this.exigidos = List.of(exigidos);
    }

    @Override
    public boolean isSatisfiedBy(SubmissaoFinanciamento s) {
        return s.documentos().primeiroFaltante(exigidos).isEmpty();
    }

    @Override
    protected DocumentoObrigatorioAusenteException violacao(SubmissaoFinanciamento s) {
        return new DocumentoObrigatorioAusenteException(s.documentos().primeiroFaltante(exigidos).orElseThrow());
    }
}
