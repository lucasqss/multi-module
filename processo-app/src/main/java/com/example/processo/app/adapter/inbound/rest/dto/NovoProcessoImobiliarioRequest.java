package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

import java.math.BigDecimal;

/** Corpo do POST /processos/imobiliarios. */
public record NovoProcessoImobiliarioRequest(ProponenteRequest proponente, BigDecimal valor, ImovelRequest imovel) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public Solicitacao<DadosImobiliarios> paraSolicitacao() {
        var i = imovel == null ? new ImovelRequest(null, null, null, null) : imovel;
        return new Solicitacao<>(ProponenteRequest.paraDominio(proponente), valor == null ? null : new Valor(valor),
                new DadosImobiliarios(i.matricula(), i.tipo(), i.valorAvaliacao(), i.uf()));
    }
}
