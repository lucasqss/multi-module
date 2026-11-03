package com.example.consorcio.domain.specification;

import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.valueobject.Dinheiro;

public class ValorDentroDoSaldoDevedorSpec implements Specification<Cota> {

    private final Dinheiro valorPretendido;

    public ValorDentroDoSaldoDevedorSpec(Dinheiro valorPretendido) {
        this.valorPretendido = valorPretendido;
    }

    @Override
    public boolean isSatisfiedBy(Cota cota) {
        return cota.comportaPagamentoDe(valorPretendido);
    }

    @Override
    public String codigo() {
        return "VALOR_EXCEDE_SALDO_DEVEDOR";
    }

    @Override
    public String mensagem(Cota cota) {
        return "Valor %s excede o saldo devedor da cota (%s)".formatted(valorPretendido, cota.saldoDevedor());
    }
}
