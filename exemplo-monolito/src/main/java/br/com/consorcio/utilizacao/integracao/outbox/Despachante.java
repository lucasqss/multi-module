package br.com.consorcio.utilizacao.integracao.outbox;

/** Sabe enviar um tipo de mensagem. Implementações ficam junto do sistema externo (core, pagamentos...). */
public interface Despachante {

    String tipo();

    /**
     * Roda dentro da transação que marca a mensagem como enviada. Pode atualizar o nosso banco
     * (ex.: ordem ENVIADA). Se lançar exceção, nada é gravado e a mensagem será repetida.
     */
    void enviar(MensagemOutbox mensagem) throws Exception;
}
