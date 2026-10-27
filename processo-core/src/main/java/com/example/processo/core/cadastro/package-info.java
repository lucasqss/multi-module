/**
 * Cadastros simples (CRUD): entidades sem fluxo, estados nem variação por tipo. Caminho curto (ADR 0012), um pacote
 * plano por cadastro: record válido por construção, repository Jakarta Data usado direto e um serviço com
 * {@code @Transactional}. Exemplo: {@code agencia}.
 *
 * <p><b>Não use aqui</b> motor, ports, fakes nem o lado de leitura separado. Se surgir fluxo, estados ou variação por
 * tipo, promova para o modelo do processo.
 */
package com.example.processo.core.cadastro;
