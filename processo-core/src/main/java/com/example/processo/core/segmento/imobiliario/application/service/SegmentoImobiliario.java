package com.example.processo.core.segmento.imobiliario.application.service;

import com.example.processo.core.motor.application.port.spi.SegmentoBase;
import com.example.processo.core.motor.domain.model.Avaliacao;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.rule.Especificacao;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.rule.RegrasImobiliarias;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.Optional;

/** Registro do segmento no motor: basta ser um bean que implementa {@code Segmento}; a base cuida do repetitivo. */
@Singleton
public class SegmentoImobiliario extends SegmentoBase<DadosImobiliarios> {

    public static final String CODIGO = "IMOBILIARIO";

    /** Acima disso o processamento recusa: o valor exige análise manual. */
    public static final BigDecimal LIMITE_AUTOMATICO = new BigDecimal("1000000.00");

    private final ProcessosImobiliarios processos;

    public SegmentoImobiliario(ProcessosImobiliarios processos) {
        super(CODIGO, "IMO", DadosImobiliarios.class, processos);
        this.processos = processos;
    }

    /** Por último a regra que vai ao banco: só consulta quando a solicitação já passou nas outras. */
    @Override
    public Especificacao<Solicitacao<DadosImobiliarios>> regras() {
        return RegrasImobiliarias.TODAS.e(this::matriculaSemProcessoEmAndamento);
    }

    @Override
    public Avaliacao avaliar(Processo<DadosImobiliarios> processo) {
        return processo.valor().quantia().compareTo(LIMITE_AUTOMATICO) > 0
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
