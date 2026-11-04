package com.example.boleto.domain.state;


/**
 * Boleto cancelado: não aceita mais nenhuma ação do usuário. Mas se o pagador pagar mesmo assim
 * (o dinheiro já entrou no sistema externo), o pagamento vai para análise.
 */
public final class EstadoCancelado extends EstadoBoleto {

    static final EstadoCancelado INSTANCIA = new EstadoCancelado();

    private EstadoCancelado() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.CANCELADO;
    }

    @Override
    public EstadoBoleto receberPagamento(boolean conforme) {
        return EstadoEmAnalise.INSTANCIA;
    }
}
