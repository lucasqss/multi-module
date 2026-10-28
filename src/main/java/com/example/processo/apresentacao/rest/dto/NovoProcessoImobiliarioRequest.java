package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.Valor;

import java.math.BigDecimal;

/** Corpo do POST /processos/imobiliarios. */
public record NovoProcessoImobiliarioRequest(ProponenteRequest proponente, BigDecimal valor, ImovelRequest imovel) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public ProcessoImobiliario paraModelo() {
        var p = proponente == null ? new ProponenteRequest(null, null) : proponente;
        var i = imovel == null ? new ImovelRequest(null, null, null, null) : imovel;
        return new ProcessoImobiliario(p.nome(), p.paraCpf(), valor == null ? null : new Valor(valor),
                i.matricula(), i.tipo(), i.valorAvaliacao(), i.uf());
    }
}
