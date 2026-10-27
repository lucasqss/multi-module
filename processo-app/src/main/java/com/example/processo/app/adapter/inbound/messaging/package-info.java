/**
 * Entrada por fila (AMQP 1.0): {@code consumer} e {@code dto} do canal. Exemplo: {@code NovosProcessosImobiliarios}.
 *
 * <ul>
 *   <li>o id da mensagem é a chave de idempotência ({@code motor.criar(solicitacao, id)}): reentrega não duplica;</li>
 *   <li>regra violada ou mensagem malformada: responde {@code recusado} e dá ack; outra falha: nack (reentrega);</li>
 *   <li>não abra transação em volta do motor; use {@code @ActivateRequestContext};</li>
 *   <li>aceite o JSON como {@code JsonObject}, texto ou bytes: depende de quem produz.</li>
 * </ul>
 */
package com.example.processo.app.adapter.inbound.messaging;
