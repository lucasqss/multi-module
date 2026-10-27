/**
 * Sistema externo de cadastro: implementa {@code SituacaoCadastral}, consultada pelo motor por último na criação. É o
 * exemplo de integração cuja resposta o fluxo precisa (falha → 503). Endereço em {@code CADASTRO_URL}; no dev,
 * {@code CadastroClientSimulado} responde sempre REGULAR.
 */
package com.example.processo.integracoes.cadastro;
