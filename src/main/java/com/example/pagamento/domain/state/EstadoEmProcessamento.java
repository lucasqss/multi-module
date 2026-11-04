package com.example.pagamento.domain.state;

/** Enviado ao banco: aguarda o retorno. O usuário não cancela (o dinheiro pode já ter saído). */
public final class EstadoEmProcessamento extends EstadoPagamento {

    static final EstadoEmProcessamento INSTANCIA = new EstadoEmProcessamento();

    private EstadoEmProcessamento() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.EM_PROCESSAMENTO;
    }

    /** Recusado: RECUSADO. Liquidado conforme: PAGO. Liquidado divergente: análise (fato, não se recusa). */
    @Override
    public EstadoPagamento receberRetornoBanco(boolean liquidado, boolean conforme) {
        if (!liquidado) {
            return EstadoRecusado.INSTANCIA;
        }
        return conforme ? EstadoPago.INSTANCIA : EstadoEmAnalise.INSTANCIA;
    }
}
