/**
 * Integrações de saída: um subpacote por sistema externo ({@code cadastro}, {@code pdf}), cada um implementando um
 * port do core (ADR 0008).
 *
 * <p>Estrutura de cada sistema:
 * <ul>
 *   <li>{@code client}: o REST Client e a simulação do perfil dev ({@code @IfBuildProfile("dev") @Alternative});</li>
 *   <li>{@code dto}: o formato do sistema externo, sem tipos do domínio (anticorrupção);</li>
 *   <li>{@code adapter}: implementa o port e traduz erro HTTP, de rede ou timeout em
 *       {@code SistemaExternoIndisponivelException}.</li>
 * </ul>
 *
 * <b>Não pode:</b> ser usado diretamente por ninguém (só pelo CDI, via port), nem ver outro sistema, nem usar
 * serviços, SPI ou adapters do core.
 */
package com.example.processo.integracoes;
