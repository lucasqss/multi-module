/**
 * Camada de negócio: a lógica do sistema. {@code processo} é o motor ({@code ProcessoService}), igual para todas as
 * modalidades; cada modalidade ({@code imobiliario}, {@code veicular}) tem um pacote com as regras e a decisão dela;
 * cadastros simples ({@code agencia}) têm um serviço CRUD.
 *
 * <ul>
 *   <li><b>Pode usar:</b> {@code integracao}, {@code persistencia} e {@code modelo}.</li>
 *   <li><b>Não pode:</b> {@code apresentacao}, nem uma modalidade usar a outra (só o motor, em {@code processo}).</li>
 *   <li><b>Transações:</b> curtas e explícitas no motor ({@code QuarkusTransaction}); {@code @Transactional} nos
 *       cadastros simples. Chamada a sistema externo nunca dentro de transação.</li>
 * </ul>
 */
package com.example.processo.negocio;
