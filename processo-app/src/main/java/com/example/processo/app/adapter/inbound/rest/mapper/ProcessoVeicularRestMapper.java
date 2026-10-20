package com.example.processo.app.adapter.inbound.rest.mapper;

import com.example.processo.app.adapter.inbound.rest.dto.NovoProcessoVeicularRequest;
import com.example.processo.app.adapter.inbound.rest.dto.VeiculoRequest;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;

/** Corpo do POST → solicitação do segmento veicular. Campos ausentes seguem nulos para as regras recusarem. */
public final class ProcessoVeicularRestMapper {

    private ProcessoVeicularRestMapper() {
    }

    public static Solicitacao<DadosVeiculares> paraSolicitacao(NovoProcessoVeicularRequest request) {
        var veiculo = request.veiculo() == null ? new VeiculoRequest(null, null, null) : request.veiculo();
        return new Solicitacao<>(ProcessoRestMapper.paraProponente(request.proponente()), request.valor(),
                new DadosVeiculares(veiculo.placa(), veiculo.anoFabricacao(), veiculo.valorFipe()));
    }
}
