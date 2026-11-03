package com.example.boleto.domain.gateway;

import com.example.boleto.domain.entity.Boleto;

/**
 * Porta de saída para um sistema EXTERNO que não é persistência (aqui: envio de e-mail/SMS).
 * {@code domain.repository} guarda as portas de dados; {@code domain.gateway}, as de integração.
 * Implementada em {@code adapter.notificacao}.
 */
public interface NotificadorBoleto {

    void notificarEmissao(Boleto boleto);
}
