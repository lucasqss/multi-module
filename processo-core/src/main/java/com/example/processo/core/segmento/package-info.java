/**
 * Um subpacote por tipo de processo ({@code imobiliario}, {@code veicular}), com a mesma estrutura do motor
 * ({@code domain}, {@code application}, {@code adapter}).
 *
 * <ul>
 *   <li><b>Pode depender de:</b> o domínio e o SPI do motor, o jpa-core.</li>
 *   <li><b>Não pode:</b> enxergar outro segmento, nem ser conhecido pelo motor (ArchUnit).</li>
 *   <li><b>Tipo novo:</b> copie {@code veicular} (o mais simples). Receita completa no {@code AGENTS.md}.</li>
 * </ul>
 */
package com.example.processo.core.segmento;
