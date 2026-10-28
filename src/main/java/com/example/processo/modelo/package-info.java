/**
 * O modelo: o vocabulário que todas as camadas usam. Entidades JPA ({@code Processo} e uma filha por modalidade,
 * herança JOINED; {@code Agencia}; tabelas só técnicas ficam na persistência), a máquina de estados ({@code Situacao}, padrão State), os value objects válidos por
 * construção ({@code Cpf}, {@code Valor}) e as exceções que atravessam as camadas.
 *
 * <ul>
 *   <li><b>Pode usar:</b> JPA (as anotações do mapeamento), nada mais.</li>
 *   <li><b>Não pode:</b> nenhuma camada, nem REST, CDI ou Quarkus.</li>
 *   <li><b>Mudou o schema?</b> Migration nova ({@code V<n>__descricao.sql}); nunca edite uma já aplicada.</li>
 * </ul>
 */
package com.example.processo.modelo;
