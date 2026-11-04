package com.example.pagamento.domain.state;

/** Enviado ao banco: aguarda o retorno. Não pode ser cancelado pelo usuário (o dinheiro pode já ter saído). */
public final class EstadoEmProcessamento extends EstadoProcesso {

    static final EstadoEmProcessamento INSTANCIA = new EstadoEmProcessamento();

    private EstadoEmProcessamento() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.EM_PROCESSAMENTO;
    }

    /** Recusado: falhou. Liquidado conforme: pago. Liquidado com divergência: análise (fato, não se recusa). */
    @Override
    public EstadoProcesso receberRetornoBanco(boolean liquidado, boolean conforme) {
        if (!liquidado) {
            return EstadoFalhou.INSTANCIA;
        }
        return conforme ? EstadoPago.INSTANCIA : EstadoEmAnalise.INSTANCIA;
    }

    /** Já foi ao banco: o pagamento pode ter acontecido para um boleto cancelado. */
    @Override
    public EstadoProcesso boletoCanceladoNoEmissor() {
        return EstadoEmAnalise.INSTANCIA;
    }
}
