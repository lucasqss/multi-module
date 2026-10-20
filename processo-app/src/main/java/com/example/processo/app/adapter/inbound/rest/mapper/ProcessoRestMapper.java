package com.example.processo.app.adapter.inbound.rest.mapper;

import com.example.processo.app.adapter.inbound.rest.dto.ProcessoCriadoResponse;
import com.example.processo.app.adapter.inbound.rest.dto.ProponenteRequest;
import com.example.processo.app.adapter.inbound.rest.dto.SituacaoResponse;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;

/** Conversões comuns a todos os segmentos: proponente na entrada, processo na saída. */
public final class ProcessoRestMapper {

    private ProcessoRestMapper() {
    }

    /** Sem proponente no corpo, segue nulo: quem recusa é a regra comum do motor, com a mensagem dela. */
    public static Proponente paraProponente(ProponenteRequest proponente) {
        return proponente == null ? null : new Proponente(proponente.nome(), proponente.cpf());
    }

    public static ProcessoCriadoResponse paraCriado(Processo<?> processo) {
        return new ProcessoCriadoResponse(processo.id(), processo.numero());
    }

    public static SituacaoResponse paraSituacao(Processo<?> processo) {
        return new SituacaoResponse(processo.situacao(), processo.motivoRecusa());
    }
}
