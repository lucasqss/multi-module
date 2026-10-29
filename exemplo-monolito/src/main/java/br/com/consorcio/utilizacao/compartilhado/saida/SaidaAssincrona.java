package br.com.consorcio.utilizacao.compartilhado.saida;

/**
 * Porta para "isto precisa sair para um sistema externo". O domínio grava a intenção na mesma
 * transação do fato de negócio; a implementação (outbox) entrega depois, com repetição segura.
 */
public interface SaidaAssincrona {

    void registrar(String tipo, Long referenciaId, String chaveIdempotencia, Object payload);
}
