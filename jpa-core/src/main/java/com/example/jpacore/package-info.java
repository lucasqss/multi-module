/**
 * O schema do banco, e só ele: entidades JPA (pai {@code ProcessoEntity} com uma filha por segmento, herança JOINED,
 * e as entidades de cadastro) e as migrations Flyway em {@code db/migration}.
 *
 * <ul>
 *   <li><b>Pode depender de:</b> Hibernate ORM, Flyway.</li>
 *   <li><b>Não pode:</b> conhecer qualquer outro módulo, nem ter regra de negócio. Nada de validação aqui.</li>
 *   <li><b>Enums de coluna</b> ({@code SituacaoProcesso}, {@code TipoImovel}) espelham os do domínio, porque este
 *       módulo não pode depender do core; o {@code EnumsEspelhadosTest} avisa se divergirem.</li>
 *   <li><b>Mudou o schema?</b> Migration nova ({@code V<n>__descricao.sql}); nunca edite uma já aplicada.</li>
 * </ul>
 *
 * Decisões: ADR 0001, 0003 e 0004 em {@code docs/adr}.
 */
package com.example.jpacore;
