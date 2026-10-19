package com.example.processo.imobiliario.application;

import com.example.processo.imobiliario.domain.DadosImobiliarios;
import com.example.processo.imobiliario.domain.ProcessosImobiliarios;
import com.example.processo.imobiliario.domain.RegrasImobiliarias;
import com.example.processo.motor.domain.Especificacao;
import com.example.processo.motor.domain.Processo;
import com.example.processo.motor.domain.Segmento;
import com.example.processo.motor.domain.Solicitacao;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/** Registro do segmento no motor: basta ser um bean que implementa {@link Segmento}. */
@ApplicationScoped
public class SegmentoImobiliario implements Segmento<DadosImobiliarios> {

    public static final String CODIGO = "IMOBILIARIO";

    private final ProcessosImobiliarios processos;

    public SegmentoImobiliario(ProcessosImobiliarios processos) {
        this.processos = processos;
    }

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String prefixo() {
        return "IMO";
    }

    @Override
    public Class<DadosImobiliarios> tipoDosDados() {
        return DadosImobiliarios.class;
    }

    /** Por último a regra que vai ao banco: só consulta quando a solicitação já passou nas outras. */
    @Override
    public Especificacao<Solicitacao<DadosImobiliarios>> regras() {
        return RegrasImobiliarias.TODAS.e(this::matriculaSemProcessoAberto);
    }

    @Override
    public void salvar(Processo<DadosImobiliarios> processo) {
        processos.salvar(processo);
    }

    private Optional<String> matriculaSemProcessoAberto(Solicitacao<DadosImobiliarios> s) {
        var imovel = s.dados();
        return processos.existeAbertoParaMatricula(imovel.matricula(), imovel.uf())
                ? Optional.of("Já existe processo aberto para a matrícula " + imovel.matricula() + "/" + imovel.uf())
                : Optional.empty();
    }
}
