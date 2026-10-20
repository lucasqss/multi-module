package com.example.processo.core.motor.adapter;

import com.example.processo.core.motor.application.Transacoes;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Supplier;

/** Cada chamada é uma transação nova, comitada ao fim do bloco (ou desfeita se ele lançar exceção). */
@ApplicationScoped
public class TransacoesQuarkus implements Transacoes {

    @Override
    public <T> T executar(Supplier<T> trabalho) {
        return QuarkusTransaction.requiringNew().call(trabalho::get);
    }
}
