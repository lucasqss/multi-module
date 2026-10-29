package br.com.consorcio.utilizacao.dominio.modalidade.imovel.modelo;

import br.com.consorcio.utilizacao.compartilhado.SimNaoConverter;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Objeto;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "imv_objeto")
@DiscriminatorValue("IMOVEL")
public class ObjetoImovel extends Objeto {

    private String matricula;

    @Column(name = "cartorio_ri")
    private String cartorioRi;

    @Column(name = "inscricao_iptu")
    private String inscricaoIptu;

    private String uf;

    private String municipio;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "construcao_averbada")
    private boolean construcaoAverbada;

    protected ObjetoImovel() {
    }

    public ObjetoImovel(String matricula, String cartorioRi, String inscricaoIptu, String uf, String municipio,
                        boolean construcaoAverbada, BigDecimal valorNegociado) {
        super("Matrícula " + matricula + " - " + cartorioRi, valorNegociado);
        this.matricula = matricula;
        this.cartorioRi = cartorioRi;
        this.inscricaoIptu = inscricaoIptu;
        this.uf = uf;
        this.municipio = municipio;
        this.construcaoAverbada = construcaoAverbada;
    }

    public String matricula() { return matricula; }

    public boolean construcaoAverbada() { return construcaoAverbada; }
}
