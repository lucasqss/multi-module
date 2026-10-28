package com.example.processo.apresentacao.mensageria.dto;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.Valor;

import java.math.BigDecimal;

/**
 * Mensagem da fila processos.imobiliarios.novos. Layout próprio do canal, plano como um registro do mainframe; não é
 * o corpo do REST. {@code id} identifica a mensagem e é a chave de idempotência: reentregar não cria outro processo.
 */
public record NovoProcessoImobiliarioMensagem(String id, String nome, String cpf, BigDecimal valor, String matricula,
                                               TipoImovel tipoImovel, BigDecimal valorAvaliacao, String uf) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public ProcessoImobiliario paraModelo() {
        return new ProcessoImobiliario(nome, new Cpf(cpf), valor == null ? null : new Valor(valor),
                matricula, tipoImovel, valorAvaliacao, uf);
    }
}
