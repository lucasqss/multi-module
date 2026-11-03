package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Pagador;

/** CANDIDATO das regras da seção de destinatários (precisa do pagador para checar repetição). */
public record DestinatariosDoBoleto(Pagador pagador, Destinatarios destinatarios) {
}
