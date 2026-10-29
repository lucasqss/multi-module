package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.*;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Raiz da hierarquia JOINED. Cada plug-in de dominio.modalidade estende esta classe com tabela própria.
 * O estado só muda por métodos com nome de negócio.
 */
@Entity
@Table(name = "prc_processo")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_processo")
public abstract class Processo {

    /** Resultado de concluir uma fase: o que o histórico precisa registrar. */
    public record Conclusao(Etapa etapa, Optional<EstadoProcesso> de, Optional<EstadoProcesso> para) {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private Finalidade finalidade;

    @Enumerated(EnumType.STRING)
    private EstadoProcesso estado = EstadoProcesso.RASCUNHO;

    @Column(name = "roteiro_codigo")
    private String roteiroCodigo;

    @Column(name = "roteiro_versao")
    private int roteiroVersao;

    /** Pessoa é de outro módulo (partes): referência por id. */
    @Column(name = "titular_pessoa_id")
    private Long titularPessoaId;

    @Column(name = "comunicacao_formal_em")
    private LocalDateTime comunicacaoFormalEm;

    @Version
    private long versao;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<Etapa> etapas = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Objeto> objetos = new ArrayList<>();

    protected Processo() {
    }

    protected Processo(Categoria categoria, Finalidade finalidade) {
        this.categoria = categoria;
        this.finalidade = finalidade;
    }

    public abstract TipoProcesso tipo();

    /** Fixa o roteiro (e a versão) e cria uma etapa por fase. */
    public void iniciar(Roteiro roteiro, Long titularPessoaId, Objeto objeto) {
        RegraNegocioException.exigir(etapas.isEmpty(), "PROCESSO_JA_INICIADO", "Processo já iniciado");
        this.roteiroCodigo = roteiro.codigo();
        this.roteiroVersao = roteiro.versao();
        this.titularPessoaId = titularPessoaId;
        int ordem = 0;
        for (FaseRoteiro f : roteiro.fases()) {
            etapas.add(new Etapa(this, f.fase(), ++ordem));
        }
        objeto.vincular(this);
        objetos.add(objeto);
    }

    /** Res. BCB 285 art. 18 §2º I: a comunicação formal inicia o prazo de análise. */
    public void registrarComunicacaoFormal() {
        comunicacaoFormalEm = LocalDateTime.now();
    }

    /**
     * Conclui a fase se as dependências estiverem resolvidas; se a fase for um portão, move o estado macro.
     * Pré-condições que dependem de outros módulos são verificadas antes, pela aplicação.
     */
    public Conclusao concluir(FaseRoteiro fr, String ator) {
        exigirNaoTerminal();
        Etapa etapa = etapa(fr.fase()).orElseThrow();
        RegraNegocioException.exigir(!etapa.situacao().resolvida(), "FASE_JA_CONCLUIDA", fr.fase() + " já foi concluída");
        List<Fase> faltando = fr.dependeDe().stream()
                .filter(dep -> !etapa(dep).orElseThrow().situacao().resolvida()).toList();
        RegraNegocioException.exigir(faltando.isEmpty(), "FASE_FORA_DE_ORDEM", fr.fase() + " depende de " + faltando);
        etapa.concluir(ator);
        if (fr.estadoAoConcluir().isEmpty()) {
            return new Conclusao(etapa, Optional.empty(), Optional.empty());
        }
        EstadoProcesso anterior = mudarEstado(fr.estadoAoConcluir().get());
        return new Conclusao(etapa, Optional.of(anterior), fr.estadoAoConcluir());
    }

    public boolean etapaPendente(Fase fase) {
        return etapa(fase).filter(e -> !e.situacao().resolvida()).isPresent();
    }

    /** Devolve o estado anterior. */
    public EstadoProcesso mudarEstado(EstadoProcesso novo) {
        RegraNegocioException.exigir(estado.proximos().contains(novo), "TRANSICAO_INVALIDA",
                "Processo " + id + " não pode ir de " + estado + " para " + novo);
        EstadoProcesso anterior = estado;
        estado = novo;
        return anterior;
    }

    public void exigirNaoTerminal() {
        RegraNegocioException.exigir(!estado.terminal(), "PROCESSO_ENCERRADO", "Processo " + id + " está " + estado);
    }

    public Objeto objeto(Long objetoId) {
        return objetos.stream().filter(o -> o.id().equals(objetoId)).findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Objeto do processo " + id, objetoId));
    }

    private Optional<Etapa> etapa(Fase fase) {
        return etapas.stream().filter(e -> e.fase() == fase).findFirst();
    }

    public Long id() { return id; }

    public Categoria categoria() { return categoria; }

    public Finalidade finalidade() { return finalidade; }

    public EstadoProcesso estado() { return estado; }

    public String roteiroCodigo() { return roteiroCodigo; }

    public int roteiroVersao() { return roteiroVersao; }

    public Long titularPessoaId() { return titularPessoaId; }

    public List<Etapa> etapas() { return Collections.unmodifiableList(etapas); }

    public List<Objeto> objetos() { return Collections.unmodifiableList(objetos); }
}
