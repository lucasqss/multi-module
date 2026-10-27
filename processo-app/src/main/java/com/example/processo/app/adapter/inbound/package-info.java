/**
 * As entradas do componente: tudo o que inicia algo, uma pasta por canal ({@code rest}, {@code messaging}; um
 * {@code scheduler} entraria aqui). Cada canal tem o seu {@code dto} e só traduz e chama um serviço do core (o motor
 * ou o serviço de um cadastro) (ADR 0011).
 *
 * <ul>
 *   <li><b>Não pode:</b> ter regra de negócio, usar ports, SPI, adapters ou entidades JPA do core, nem usar classes
 *       de outro canal (ArchUnit).</li>
 *   <li><b>Fora do HTTP</b> (fila, agendamento) use {@code @ActivateRequestContext}.</li>
 *   <li>Canal com dependência pesada ou descartável vira módulo de entrada próprio.</li>
 * </ul>
 */
package com.example.processo.app.adapter.inbound;
