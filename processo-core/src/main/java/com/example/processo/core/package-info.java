/**
 * Toda a regra de negócio da escrita, sem HTTP. Três áreas, que não se misturam:
 *
 * <ul>
 *   <li>{@code motor}: o fluxo único de todos os tipos de processo (criar, processar, cancelar) e a máquina de
 *       estados;</li>
 *   <li>{@code segmento.<tipo>}: o que muda de um tipo de processo para outro (dados, regras, decisão do
 *       processamento, persistência da tabela filha);</li>
 *   <li>{@code cadastro.<nome>}: cadastros simples (CRUD), pelo caminho curto, sem motor.</li>
 * </ul>
 *
 * Quem está de fora (REST, fila) entra só pelos serviços: {@code MotorDeProcessamento} ou o serviço de um cadastro.
 * As fronteiras são garantidas pelo {@code ArquiteturaTest} (aqui) e pelo {@code ArquiteturaModulosTest} (no app).
 */
package com.example.processo.core;
