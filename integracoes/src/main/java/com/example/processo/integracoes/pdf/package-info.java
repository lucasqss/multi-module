/**
 * Sistema externo de geração de PDF: implementa {@code GeradorDeDocumentos}, chamado pelo motor depois do commit da
 * criação. É o exemplo de integração que não pode desfazer o fluxo: a falha é só registrada no log. Endereço em
 * {@code SISTEMA_PDF_URL}, timeout de 2 s; no dev, {@code SistemaDePdfSimulado} só registra o pedido.
 */
package com.example.processo.integracoes.pdf;
