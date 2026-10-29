package br.com.consorcio.utilizacao.dominio.modalidade.servico.modelo;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Objeto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "svc_objeto")
@DiscriminatorValue("SERVICO")
public class ObjetoServico extends Objeto {

    private String escopo;

    @Column(name = "prazo_execucao")
    private LocalDate prazoExecucao;

    protected ObjetoServico() {
    }

    public ObjetoServico(String escopo, LocalDate prazoExecucao, BigDecimal valorNegociado) {
        super(escopo, valorNegociado);
        this.escopo = escopo;
        this.prazoExecucao = prazoExecucao;
    }
}
