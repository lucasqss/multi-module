package com.example.pagamento.domain.valueobject;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

/** Seção "boletos" do pagamento: substituída inteira a cada salvamento. O total é a soma dos boletos. */
public record BoletosDoPagamento(List<BoletoAPagar> boletos) {

    public static final BoletosDoPagamento NENHUM = new BoletosDoPagamento(List.of());

    public BoletosDoPagamento {
        boletos = List.copyOf(boletos);
    }

    public Dinheiro total() {
        return new Dinheiro(boletos.stream().map(b -> b.valor().valor()).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public int quantidade() {
        return boletos.size();
    }

    public boolean vazio() {
        return boletos.isEmpty();
    }

    public boolean possuiRepetidos() {
        return new HashSet<>(boletos.stream().map(BoletoAPagar::idNoEmissor).toList()).size() != boletos.size();
    }

    public boolean mesmoBeneficiario() {
        return boletos.stream().map(BoletoAPagar::beneficiario).distinct().count() <= 1;
    }
}
