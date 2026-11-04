package com.example.financiamento.domain.specification.financiamento;

import com.example.financiamento.domain.valueobject.Bem;
import com.example.financiamento.domain.valueobject.RetornoRegistro;

/** CANDIDATO da conformidade do registro: o bem do contrato e o que o órgão informou ter registrado. */
public record ConferenciaRegistro(Bem bem, RetornoRegistro retorno) {
}
