package com.example.consorcio.entrypoint.api.v1.handler;

import com.example.consorcio.domain.exception.Violacao;

import java.util.List;

public record ErroResponse(String codigo, String mensagem, List<Violacao> violacoes) {
}
