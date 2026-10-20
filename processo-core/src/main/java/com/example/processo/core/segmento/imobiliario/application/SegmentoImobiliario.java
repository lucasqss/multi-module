package com.example.processo.core.segmento.imobiliario.application;

import com.example.processo.core.segmento.imobiliario.domain.DadosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.ProcessosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.RegrasImobiliarias;
import com.example.processo.core.motor.domain.Avaliacao;
import com.example.processo.core.motor.domain.Especificacao;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.Segmento;
import com.example.processo.core.motor.domain.Solicitacao;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/** Registro do segmento no motor: basta ser um bean que implementa {@link Segmento}. */
@ApplicationScoped
public class SegmentoImobiliario implements Segmento<DadosImobiliarios> {

    public static final String CODIGO = "IMOBILIARIO";

    /** Acima disso o processamento recusa: o valor exige análise manual. */
    public static final BigDecimal LIMITE_AUTOMATICO = new BigDecimal("1000000.00");

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
        return RegrasImobiliarias.TODAS.e(this::matriculaSemProcessoEmAndamento);
    }

    @Override
    public void salvar(Processo<DadosImobiliarios> processo) {
        processos.salvar(processo);
    }

    @Override
    public Optional<Processo<DadosImobiliarios>> carregar(UUID id) {
        return processos.porId(id);
    }

    @Override
    public Avaliacao avaliar(Processo<DadosImobiliarios> processo) {
        return processo.valor().compareTo(LIMITE_AUTOMATICO) > 0
                ? Avaliacao.recusado("Valor acima de " + LIMITE_AUTOMATICO.toPlainString() + " exige análise manual")
                : Avaliacao.aprovado();
    }

    private Optional<String> matriculaSemProcessoEmAndamento(Solicitacao<DadosImobiliarios> s) {
        var imovel = s.dados();
        return processos.existeEmAndamentoParaMatricula(imovel.matricula(), imovel.uf())
                ? Optional.of("Já existe processo em andamento para a matrícula " + imovel.matricula() + "/" + imovel.uf())
                : Optional.empty();
    }
}
