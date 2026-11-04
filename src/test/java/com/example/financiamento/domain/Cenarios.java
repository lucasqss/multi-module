package com.example.financiamento.domain;

import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.specification.financiamento.SubmissaoFinanciamento;
import com.example.financiamento.domain.valueobject.Bem;
import com.example.financiamento.domain.valueobject.Cpf;
import com.example.financiamento.domain.valueobject.Dinheiro;
import com.example.financiamento.domain.valueobject.Documento;
import com.example.financiamento.domain.valueobject.DocumentosDoFinanciamento;
import com.example.financiamento.domain.valueobject.IdProtocolo;
import com.example.financiamento.domain.valueobject.Proponente;
import com.example.financiamento.domain.valueobject.RetornoRegistro;
import com.example.financiamento.domain.valueobject.TipoDocumento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.financiamento.domain.valueobject.TipoDocumento.COMPROVANTE_RENDA;
import static com.example.financiamento.domain.valueobject.TipoDocumento.CRLV;
import static com.example.financiamento.domain.valueobject.TipoDocumento.IDENTIDADE;
import static com.example.financiamento.domain.valueobject.TipoDocumento.MATRICULA_IMOVEL;

/** Test data builder: financiamento VÁLIDO (pronto para submeter) por padrão; cada teste muda só o que interessa. */
public final class Cenarios {

    public static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    public static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");

    private final TipoFinanciamento tipo;
    private Proponente proponente;
    private Dinheiro valorSolicitado;
    private Integer prazoMeses;
    private Bem bem;
    private final List<Documento> documentos = new ArrayList<>();

    private Cenarios(TipoFinanciamento tipo, String renda, String valor, int prazo, Bem bem, TipoDocumento... docs) {
        this.tipo = tipo;
        this.proponente = new Proponente("Maria Silva", new Cpf("12345678901"), Dinheiro.de(renda));
        this.valorSolicitado = Dinheiro.de(valor);
        this.prazoMeses = prazo;
        this.bem = bem;
        for (var d : docs) {
            documentos.add(new Documento(d, "DOC-" + d));
        }
    }

    /** IMÓVEL de R$ 500.000; pede R$ 300.000 em 360 meses; renda de R$ 20.000. */
    public static Cenarios imovel() {
        return new Cenarios(TipoFinanciamento.IMOVEL, "20000.00", "300000.00", 360,
                imovelAvaliadoEm("500000.00"), IDENTIDADE, COMPROVANTE_RENDA, MATRICULA_IMOVEL);
    }

    /** VEÍCULO de R$ 100.000, ano 2023; pede R$ 60.000 em 48 meses; renda de R$ 10.000. */
    public static Cenarios veiculo() {
        return new Cenarios(TipoFinanciamento.VEICULO, "10000.00", "60000.00", 48,
                veiculoAno(2023), IDENTIDADE, COMPROVANTE_RENDA, CRLV);
    }

    public static Bem imovelAvaliadoEm(String valor) {
        return new Bem("500", "Imóvel da matrícula 500", Dinheiro.de(valor), null);
    }

    public static Bem veiculoAno(int ano) {
        return new Bem("ABC1D23", "Veículo placa ABC1D23", Dinheiro.de("100000.00"), ano);
    }

    public Cenarios semProponente() { proponente = null; return this; }
    public Cenarios renda(String renda) { proponente = new Proponente("Maria Silva", new Cpf("12345678901"), Dinheiro.de(renda)); return this; }
    public Cenarios valor(String valor) { valorSolicitado = Dinheiro.de(valor); return this; }
    public Cenarios prazo(int meses) { prazoMeses = meses; return this; }
    public Cenarios semCondicoes() { valorSolicitado = null; prazoMeses = null; return this; }
    public Cenarios bem(Bem novo) { bem = novo; return this; }
    public Cenarios semDocumento(TipoDocumento d) { documentos.removeIf(doc -> doc.tipo() == d); return this; }

    public DocumentosDoFinanciamento documentos() {
        return new DocumentosDoFinanciamento(documentos);
    }

    public SubmissaoFinanciamento submissao() {
        return new SubmissaoFinanciamento(tipo, proponente, valorSolicitado, prazoMeses, bem, documentos(), HOJE);
    }

    /** Rascunho (EM_EDICAO) com as seções preenchidas. */
    public Financiamento rascunho() {
        var f = Financiamento.criar(tipo, AGORA);
        f.alterarDadosGerais(proponente, valorSolicitado, prazoMeses);
        if (bem != null) {
            f.informarGarantia(bem);
        }
        f.salvarDocumentos(documentos());
        return f;
    }

    public Financiamento aprovado() {
        var f = rascunho();
        f.submeter(HOJE);
        return f;
    }

    public Financiamento emRegistro() {
        var f = aprovado();
        f.contratar();
        return f;
    }

    public static RetornoRegistro registrado(Financiamento f, String identificacaoRegistrada) {
        return new RetornoRegistro(protocolo(f), true, identificacaoRegistrada, "REG-1", null, AGORA);
    }

    public static RetornoRegistro recusado(Financiamento f) {
        return new RetornoRegistro(protocolo(f), false, null, null, "documentação incompleta", AGORA);
    }

    private static IdProtocolo protocolo(Financiamento f) {
        return f.getIdProtocolo().orElseThrow();
    }
}
