package com.example.processo.negocio.veicular;

import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.negocio.processo.Avaliacao;
import com.example.processo.negocio.processo.Especificacao;
import com.example.processo.negocio.processo.Modalidade;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Clock;
import java.time.LocalDate;

/** A modalidade veicular no motor: basta ser um bean que implementa {@link Modalidade}. */
@ApplicationScoped
public class ModalidadeVeicular implements Modalidade<ProcessoVeicular> {

    /** Veículo mais velho que isso é recusado no processamento. */
    public static final int IDADE_MAXIMA = 10;

    private final Clock relogio;

    public ModalidadeVeicular(Clock relogio) {
        this.relogio = relogio;
    }

    @Override
    public Class<ProcessoVeicular> tipo() {
        return ProcessoVeicular.class;
    }

    @Override
    public String prefixo() {
        return "VEI";
    }

    @Override
    public Especificacao<ProcessoVeicular> regras() {
        return RegrasVeiculares.TODAS;
    }

    /** A idade conta na data do processamento, não na da abertura. */
    @Override
    public Avaliacao avaliar(ProcessoVeicular processo) {
        var idade = LocalDate.now(relogio).getYear() - processo.getAnoFabricacao();
        return idade > IDADE_MAXIMA
                ? Avaliacao.recusado("Veículo com mais de " + IDADE_MAXIMA + " anos (fabricado em "
                        + processo.getAnoFabricacao() + ")")
                : Avaliacao.aprovado();
    }
}
