package com.example.processo.app.adapter.inbound.rest.mapper;

import com.example.processo.app.adapter.inbound.rest.dto.ImovelRequest;
import com.example.processo.app.adapter.inbound.rest.dto.NovoProcessoImobiliarioRequest;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

/** Corpo do POST → solicitação do segmento imobiliário. Campos ausentes seguem nulos para as regras recusarem. */
public final class ProcessoImobiliarioRestMapper {

    private ProcessoImobiliarioRestMapper() {
    }

    public static Solicitacao<DadosImobiliarios> paraSolicitacao(NovoProcessoImobiliarioRequest request) {
        var imovel = request.imovel() == null ? new ImovelRequest(null, null, null, null) : request.imovel();
        return new Solicitacao<>(ProcessoRestMapper.paraProponente(request.proponente()), request.valor(),
                new DadosImobiliarios(imovel.matricula(), imovel.tipo(), imovel.valorAvaliacao(), imovel.uf()));
    }
}
