/**
 * Camada de apresentação: por onde os pedidos chegam e as respostas saem. Um pacote por canal ({@code rest},
 * {@code mensageria}), cada um com o seu {@code dto}. Só traduz: converte o pedido para o modelo, chama o negócio e
 * converte o resultado para a resposta. Regra de negócio aqui não.
 *
 * <ul>
 *   <li><b>Pode usar:</b> {@code negocio} (para gravar e processar), {@code persistencia.consulta} (para ler, sem
 *       serviço no meio) e {@code modelo}.</li>
 *   <li><b>Não pode:</b> {@code persistencia.repositorio}, {@code integracao}, nem um canal usar o outro.</li>
 * </ul>
 *
 * Regras em {@code ArquiteturaTest}; decisão na ADR 0014.
 */
package com.example.processo.apresentacao;
