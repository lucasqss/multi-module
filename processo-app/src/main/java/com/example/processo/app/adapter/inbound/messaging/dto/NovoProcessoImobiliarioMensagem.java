package com.example.processo.app.adapter.inbound.messaging.dto;

import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

import java.math.BigDecimal;

/**
 * Mensagem da fila processos.imobiliarios.novos. Layout próprio do canal, plano como um registro do mainframe; não é
 * o corpo do REST. {@code id} identifica a mensagem e é a chave de idempotência: reentregar não cria outro processo.
 */
public record NovoProcessoImobiliarioMensagem(String id, String nome, String cpf, BigDecimal valor, String matricula,
                                               DadosImobiliarios.TipoImovel tipoImovel, BigDecimal valorAvaliacao,
                                               String uf) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public Solicitacao<DadosImobiliarios> paraSolicitacao() {
        return new Solicitacao<>(new Proponente(nome, cpf), valor == null ? null : new Valor(valor),
                new DadosImobiliarios(matricula, tipoImovel, valorAvaliacao, uf));
    }
}
