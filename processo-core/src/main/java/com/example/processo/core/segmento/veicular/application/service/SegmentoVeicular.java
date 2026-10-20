package com.example.processo.core.segmento.veicular.application.service;

import com.example.processo.core.motor.application.port.spi.Segmento;
import com.example.processo.core.motor.domain.model.Avaliacao;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.rule.Especificacao;
import com.example.processo.core.segmento.veicular.application.port.out.ProcessosVeiculares;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;
import com.example.processo.core.segmento.veicular.domain.rule.RegrasVeiculares;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/** Registro do segmento no motor: basta ser um bean que implementa {@link Segmento}. */
@ApplicationScoped
public class SegmentoVeicular implements Segmento<DadosVeiculares> {

    public static final String CODIGO = "VEICULAR";

    /** Veículo mais velho que isso é recusado no processamento. */
    public static final int IDADE_MAXIMA = 10;

    private final ProcessosVeiculares processos;
    private final Clock relogio;

    public SegmentoVeicular(ProcessosVeiculares processos, Clock relogio) {
        this.processos = processos;
        this.relogio = relogio;
    }

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String prefixo() {
        return "VEI";
    }

    @Override
    public Class<DadosVeiculares> tipoDosDados() {
        return DadosVeiculares.class;
    }

    @Override
    public Especificacao<Solicitacao<DadosVeiculares>> regras() {
        return RegrasVeiculares.TODAS;
    }

    @Override
    public void salvar(Processo<DadosVeiculares> processo) {
        processos.salvar(processo);
    }

    @Override
    public Optional<Processo<DadosVeiculares>> carregar(UUID id) {
        return processos.porId(id);
    }

    /** A idade conta na data do processamento, não na da abertura. */
    @Override
    public Avaliacao avaliar(Processo<DadosVeiculares> processo) {
        var idade = LocalDate.now(relogio).getYear() - processo.dados().anoFabricacao();
        return idade > IDADE_MAXIMA
                ? Avaliacao.recusado("Veículo com mais de " + IDADE_MAXIMA + " anos (fabricado em "
                        + processo.dados().anoFabricacao() + ")")
                : Avaliacao.aprovado();
    }
}
