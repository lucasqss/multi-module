package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.Valor;

import java.math.BigDecimal;

/** Corpo do POST /processos/veiculares. */
public record NovoProcessoVeicularRequest(ProponenteRequest proponente, BigDecimal valor, VeiculoRequest veiculo) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public ProcessoVeicular paraModelo() {
        var p = proponente == null ? new ProponenteRequest(null, null) : proponente;
        var v = veiculo == null ? new VeiculoRequest(null, null, null) : veiculo;
        return new ProcessoVeicular(p.nome(), p.paraCpf(), valor == null ? null : new Valor(valor),
                v.placa(), v.anoFabricacao(), v.valorFipe());
    }
}
