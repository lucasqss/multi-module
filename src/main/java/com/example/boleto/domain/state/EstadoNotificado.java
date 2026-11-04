package com.example.boleto.domain.state;


/**
 * O pagador já recebeu o boleto: não pode mais ser alterado nem excluído
 * (para mudar algo, cancele e emita outro). Aguarda o pagamento.
 */
public final class EstadoNotificado extends EstadoBoleto {

    static final EstadoNotificado INSTANCIA = new EstadoNotificado();

    private EstadoNotificado() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.NOTIFICADO;
    }

    /** Reenvio da notificação: transição para o PRÓPRIO estado. */
    @Override
    public EstadoBoleto notificar() {
        return this;
    }

    /** Conforme quita; divergente vai para análise (o dinheiro já entrou, não dá para recusar). */
    @Override
    public EstadoBoleto receberPagamento(boolean conforme) {
        return conforme ? EstadoPago.INSTANCIA : EstadoEmAnalise.INSTANCIA;
    }

    @Override
    public EstadoBoleto cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    @Override
    public boolean emAberto() {
        return true;
    }
}
