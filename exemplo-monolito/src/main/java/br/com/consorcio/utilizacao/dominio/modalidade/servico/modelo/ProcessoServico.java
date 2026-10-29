package br.com.consorcio.utilizacao.dominio.modalidade.servico.modelo;

import br.com.consorcio.utilizacao.compartilhado.SimNaoConverter;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Categoria;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.TipoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;
import jakarta.persistence.*;

@Entity
@Table(name = "svc_processo")
@DiscriminatorValue("SERVICO")
public class ProcessoServico extends Processo {

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "possui_cronograma")
    private boolean possuiCronograma;

    protected ProcessoServico() {
    }

    public ProcessoServico(boolean possuiCronograma) {
        super(Categoria.SERVICO, Finalidade.AQUISICAO);
        this.possuiCronograma = possuiCronograma;
    }

    @Override
    public TipoProcesso tipo() {
        return TipoProcesso.SERVICO;
    }
}
