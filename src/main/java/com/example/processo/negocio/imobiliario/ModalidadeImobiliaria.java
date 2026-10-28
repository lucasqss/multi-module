package com.example.processo.negocio.imobiliario;

import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.negocio.processo.Avaliacao;
import com.example.processo.negocio.processo.Especificacao;
import com.example.processo.negocio.processo.Modalidade;
import com.example.processo.persistencia.repositorio.ProcessoImobiliarioRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.Optional;

/** A modalidade imobiliária no motor: basta ser um bean que implementa {@link Modalidade}. */
@ApplicationScoped
public class ModalidadeImobiliaria implements Modalidade<ProcessoImobiliario> {

    /** Acima disso o processamento recusa: o valor exige análise manual. */
    public static final BigDecimal LIMITE_AUTOMATICO = new BigDecimal("1000000.00");

    private final ProcessoImobiliarioRepository repositorio;

    public ModalidadeImobiliaria(ProcessoImobiliarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Class<ProcessoImobiliario> tipo() {
        return ProcessoImobiliario.class;
    }

    @Override
    public String prefixo() {
        return "IMO";
    }

    /** Por último a regra que vai ao banco: só consulta quando o pedido já passou nas outras. */
    @Override
    public Especificacao<ProcessoImobiliario> regras() {
        return RegrasImobiliarias.TODAS.e(this::matriculaSemProcessoEmAndamento);
    }

    @Override
    public Avaliacao avaliar(ProcessoImobiliario processo) {
        return processo.getValor().compareTo(LIMITE_AUTOMATICO) > 0
                ? Avaliacao.recusado("Valor acima de " + LIMITE_AUTOMATICO.toPlainString() + " exige análise manual")
                : Avaliacao.aprovado();
    }

    private Optional<String> matriculaSemProcessoEmAndamento(ProcessoImobiliario p) {
        return repositorio.existeEmAndamentoParaMatricula(p.getMatricula(), p.getUf())
                ? Optional.of("Já existe processo em andamento para a matrícula " + p.getMatricula() + "/" + p.getUf())
                : Optional.empty();
    }
}
