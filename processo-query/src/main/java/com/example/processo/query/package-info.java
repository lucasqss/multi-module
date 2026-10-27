/**
 * O lado de leitura dos processos: consultas somente leitura que projetam direto das entidades do jpa-core
 * ({@code select new}) para records em {@code dto}, que já são o contrato de resposta dos GETs.
 *
 * <ul>
 *   <li><b>Pode depender de:</b> jpa-core, Jakarta Data.</li>
 *   <li><b>Não pode:</b> depender do {@code processo-core}, e o core também não depende daqui. Os dois só se encontram nos
 *       resources do app (ADR 0004).</li>
 *   <li><b>Projeções</b> são records planos e top-level: {@code select new} não instancia records aninhados.</li>
 * </ul>
 */
package com.example.processo.query;
