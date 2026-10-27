/**
 * Entrada HTTP: {@code resource} (endpoints), {@code dto} (requests e responses, com a conversão de/para o domínio,
 * ex.: {@code paraSolicitacao()}) e {@code exception} (o {@code TratadorDeErros}: exceção → Problem Details, ADR 0010).
 *
 * <p>Os GETs de processo devolvem as projeções do {@code processo-query}; os de cadastro, o próprio record. Erro novo do
 * domínio = uma exceção no core e um método no {@code TratadorDeErros}.
 */
package com.example.processo.app.adapter.inbound.rest;
