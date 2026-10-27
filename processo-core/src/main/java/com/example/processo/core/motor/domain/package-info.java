/**
 * O domínio do processo, em Java puro: nenhuma anotação de framework, nenhum acesso a banco ou rede.
 *
 * <ul>
 *   <li>{@code model}: {@code Processo<D>} (genérico nos dados do segmento), {@code Solicitacao<D>} e os value objects
 *       {@code Cpf} e {@code Valor}, válidos por construção (inválido lança {@code RegraVioladaException});</li>
 *   <li>{@code state}: a máquina de estados, padrão State num enum ({@code Situacao});</li>
 *   <li>{@code rule}: {@code Especificacao} e as regras comuns a todos os segmentos;</li>
 *   <li>{@code exception}: exceções de negócio; cada uma vira um Problem Details no {@code TratadorDeErros} do app.</li>
 * </ul>
 *
 * Mensagens de exceção são apresentáveis ao usuário: viram o {@code detail} da resposta HTTP ou o {@code motivo} da
 * resposta da fila.
 */
package com.example.processo.core.motor.domain;
