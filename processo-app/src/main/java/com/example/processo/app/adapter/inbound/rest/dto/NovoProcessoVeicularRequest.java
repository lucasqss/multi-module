package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;

import java.math.BigDecimal;

/** Corpo do POST /processos/veiculares. */
public record NovoProcessoVeicularRequest(ProponenteRequest proponente, BigDecimal valor, VeiculoRequest veiculo) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public Solicitacao<DadosVeiculares> paraSolicitacao() {
        var v = veiculo == null ? new VeiculoRequest(null, null, null) : veiculo;
        return new Solicitacao<>(ProponenteRequest.paraDominio(proponente), valor == null ? null : new Valor(valor),
                new DadosVeiculares(v.placa(), v.anoFabricacao(), v.valorFipe()));
    }
}
