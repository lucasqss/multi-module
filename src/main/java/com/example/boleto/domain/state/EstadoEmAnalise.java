package com.example.boleto.domain.state;

/**
 * Pagamento recebido, mas fora da conformidade (valor diferente, depois do vencimento, boleto cancelado...).
 * O financeiro decide: aceitar como quitação ou devolver o dinheiro ao pagador.
 */
public final class EstadoEmAnalise extends EstadoBoleto {

    static final EstadoEmAnalise INSTANCIA = new EstadoEmAnalise();

    private EstadoEmAnalise() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.EM_ANALISE;
    }

    @Override
    public EstadoBoleto aceitarPagamento() {
        return EstadoPago.INSTANCIA;
    }

    @Override
    public EstadoBoleto devolverPagamento() {
        return EstadoDevolvido.INSTANCIA;
    }
}
