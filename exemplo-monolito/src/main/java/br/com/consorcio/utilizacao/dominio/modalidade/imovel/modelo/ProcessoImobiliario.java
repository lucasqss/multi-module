package br.com.consorcio.utilizacao.dominio.modalidade.imovel.modelo;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Categoria;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Finalidade;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.TipoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;
import jakarta.persistence.*;

/** Plug-in imóvel: subtabela JOINED de prc_processo. Única dependência de entidade entre módulos permitida. */
@Entity
@Table(name = "imv_processo")
@DiscriminatorValue("IMOBILIARIO")
public class ProcessoImobiliario extends Processo {

    @Column(name = "tipo_operacao")
    private String tipoOperacao;

    @Column(name = "municipio_itbi")
    private String municipioItbi;

    protected ProcessoImobiliario() {
    }

    public ProcessoImobiliario(Finalidade finalidade, String tipoOperacao, String municipioItbi) {
        super(Categoria.IMOVEL, finalidade);
        this.tipoOperacao = tipoOperacao;
        this.municipioItbi = municipioItbi;
    }

    @Override
    public TipoProcesso tipo() {
        return TipoProcesso.IMOBILIARIO;
    }

    public String tipoOperacao() { return tipoOperacao; }

    public String municipioItbi() { return municipioItbi; }
}
