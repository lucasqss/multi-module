package com.example.financiamento.domain.entity;

import com.example.financiamento.domain.specification.financiamento.ConferenciaRegistro;
import com.example.financiamento.domain.specification.financiamento.RegrasFinanciamento;
import com.example.financiamento.domain.specification.financiamento.SubmissaoFinanciamento;
import com.example.financiamento.domain.state.StatusFinanciamento;
import com.example.financiamento.domain.valueobject.Bem;
import com.example.financiamento.domain.valueobject.Dinheiro;
import com.example.financiamento.domain.valueobject.Documento;
import com.example.financiamento.domain.valueobject.DocumentosDoFinanciamento;
import com.example.financiamento.domain.valueobject.IdProtocolo;
import com.example.financiamento.domain.valueobject.Motivo;
import com.example.financiamento.domain.valueobject.MotivoPendencia;
import com.example.financiamento.domain.valueobject.Proponente;
import com.example.financiamento.domain.valueobject.RetornoRegistro;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root: financiamento de um bem (imóvel ou veículo), que é a própria GARANTIA do contrato.
 * O mesmo agregado e a mesma máquina de estados servem aos dois tipos; o que varia por {@link TipoFinanciamento}
 * (prazo, percentual financiável, documentos, idade do veículo, órgão de registro) fica nos elos da cadeia de
 * {@link RegrasFinanciamento} e no próprio tipo — não em {@code if}s aqui.
 *
 * <p>Domínio RICO e entidade JPA ao mesmo tempo: campos privados (acesso por campo), construtor sem
 * argumentos só para o JPA, nenhum setter. O estado só muda por métodos de negócio, sempre nesta ordem:
 * 1º o STATUS diz se a ação é permitida (409); 2º as REGRAS validam (422) ou, para fatos externos,
 * classificam; 3º só então muda. A entidade carregada é gerenciada: o UPDATE sai no commit.
 */
@Entity
@Table(name = "financiamento", indexes = @Index(name = "ux_financiamento_protocolo", columnList = "id_protocolo", unique = true))
public class Financiamento {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoFinanciamento tipo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    // ---------- seção "dados gerais" ----------

    @Embedded
    @AttributeOverride(name = "nome", column = @Column(name = "proponente_nome", length = 120))
    @AttributeOverride(name = "cpf", column = @Column(name = "proponente_cpf", length = 11))
    @AttributeOverride(name = "rendaMensal", column = @Column(name = "proponente_renda", precision = 15, scale = 2))
    private Proponente proponente;

    @Column(name = "valor_solicitado", precision = 15, scale = 2)
    private Dinheiro valorSolicitado;

    @Column(name = "prazo_meses")
    private Integer prazoMeses;

    // ---------- seção "garantia" ----------

    @Embedded
    @AttributeOverride(name = "identificacao", column = @Column(name = "bem_identificacao", length = 20))
    @AttributeOverride(name = "descricao", column = @Column(name = "bem_descricao", length = 200))
    @AttributeOverride(name = "valorAvaliado", column = @Column(name = "bem_valor_avaliado", precision = 15, scale = 2))
    @AttributeOverride(name = "anoModelo", column = @Column(name = "bem_ano_modelo"))
    private Bem bem;

    // ---------- seção "documentos" ----------

    /**
     * LAZY porque FOR UPDATE não combina com join de coleção; {@code @BatchSize}: na listagem, os documentos de
     * vários financiamentos vêm numa consulta só (o padrão do Quarkus é 16; 100 = página máxima).
     * Coleção gerenciada: nunca trocar a lista, só limpar e repovoar.
     */
    @ElementCollection
    @CollectionTable(name = "financiamento_documento", joinColumns = @JoinColumn(name = "financiamento_id"))
    @OrderColumn(name = "ordem")
    @BatchSize(size = 100)
    private List<Documento> documentos = new ArrayList<>();

    // ---------- ciclo de vida ----------

    /** State pattern: o enum tem o comportamento de cada status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusFinanciamento status;

    @Column(nullable = false)
    private int envios;

    @Column(name = "id_protocolo", length = 50)
    private IdProtocolo idProtocolo;

    @Column(name = "numero_registro", length = 60)
    private String numeroRegistro;

    @Column(name = "motivo_recusa")
    private String motivoRecusa;

    @Embedded
    @AttributeOverride(name = "codigo", column = @Column(name = "motivo_pendencia_codigo", length = 60))
    @AttributeOverride(name = "mensagem", column = @Column(name = "motivo_pendencia_mensagem"))
    private MotivoPendencia motivoPendencia;

    @Column(name = "motivo_cancelamento")
    private Motivo motivoCancelamento;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    /** Só para o JPA. */
    protected Financiamento() {
    }

    private Financiamento(TipoFinanciamento tipo, Instant criadoEm) {
        this.id = UUID.randomUUID();
        this.tipo = Objects.requireNonNull(tipo);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.status = StatusFinanciamento.EM_EDICAO;
    }

    // =============================== criação ===============================

    /** Cria o rascunho (EM_EDICAO) só com o tipo. Completude é exigida em {@link #submeter}. */
    public static Financiamento criar(TipoFinanciamento tipo, Instant agora) {
        return new Financiamento(tipo, agora);
    }

    // =============================== edição por partes (só em EM_EDICAO) ===============================

    /** Seção "dados gerais". Tudo opcional até a submissão. */
    public void alterarDadosGerais(Proponente novoProponente, Dinheiro novoValor, Integer novoPrazo) {
        status.validarEdicao();
        this.proponente = novoProponente;
        this.valorSolicitado = novoValor;
        this.prazoMeses = novoPrazo;
    }

    /** Seção "garantia": o bem como o sistema de avaliação devolveu. */
    public void informarGarantia(Bem avaliado) {
        status.validarEdicao();
        this.bem = Objects.requireNonNull(avaliado);
    }

    /** Seção "documentos": substitui inteira. Só as regras da seção; o financiamento pode continuar incompleto. */
    public void salvarDocumentos(DocumentosDoFinanciamento novos) {
        status.validarEdicao();
        RegrasFinanciamento.DOCUMENTOS.validar(novos);
        documentos.clear();
        documentos.addAll(novos.documentos());
    }

    public void validarExclusao() {
        status.validarExclusao();
    }

    // =============================== transições (comandos do usuário) ===============================

    /** Finaliza a edição: completude + crédito + regras do tipo (Chain of Responsibility). EM_EDICAO -> APROVADO. */
    public void submeter(LocalDate hoje) {
        var proximo = status.submeter();
        RegrasFinanciamento.SUBMISSAO.validar(new SubmissaoFinanciamento(tipo, proponente, valorSolicitado, prazoMeses,
                bem, getDocumentos(), hoje));
        status = proximo;
    }

    /** APROVADO -> EM_REGISTRO, com um novo protocolo (o id é nosso; vai ao órgão e volta no retorno). */
    public void contratar() {
        status = status.contratar();
        novoProtocolo();
    }

    /** REGISTRO_RECUSADO -> EM_REGISTRO, com outro protocolo. */
    public void reenviarRegistro() {
        status = status.reenviarRegistro();
        novoProtocolo();
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        status = status.cancelar();
        motivoCancelamento = motivo;
    }

    // =============================== fato externo (retorno do registro) ===============================

    /**
     * O órgão informa o resultado do registro. É um FATO: a conformidade CLASSIFICA (não lança).
     * Idempotente: retorno repetido, ou de um protocolo que não é o atual (envio antigo), é ignorado.
     */
    public void registrarRetornoRegistro(RetornoRegistro retorno) {
        if (!retorno.idProtocolo().equals(idProtocolo) || numeroRegistro != null || motivoRecusa != null) {
            return; // aviso repetido ou de outro protocolo: já processado
        }
        var divergencia = RegrasFinanciamento.CONFORMIDADE_REGISTRO.primeiraViolacao(new ConferenciaRegistro(bem, retorno));

        status = status.receberRetornoRegistro(retorno.registrado(), divergencia.isEmpty());
        numeroRegistro = retorno.numeroRegistro();
        motivoRecusa = retorno.motivoRecusa();
        motivoPendencia = divergencia.map(MotivoPendencia::de).orElse(null);
    }

    private void novoProtocolo() {
        envios++;
        idProtocolo = new IdProtocolo(id, envios);
        numeroRegistro = null;
        motivoRecusa = null;
    }

    // =============================== leitura ===============================

    public UUID getId() { return id; }
    public TipoFinanciamento getTipo() { return tipo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Optional<Proponente> getProponente() { return Optional.ofNullable(proponente); }
    public Optional<Dinheiro> getValorSolicitado() { return Optional.ofNullable(valorSolicitado); }
    public Optional<Integer> getPrazoMeses() { return Optional.ofNullable(prazoMeses); }
    public Optional<Bem> getBem() { return Optional.ofNullable(bem); }
    public DocumentosDoFinanciamento getDocumentos() { return new DocumentosDoFinanciamento(documentos); }
    public StatusFinanciamento getStatus() { return status; }
    public int getEnvios() { return envios; }
    public Optional<IdProtocolo> getIdProtocolo() { return Optional.ofNullable(idProtocolo); }
    public Optional<String> getNumeroRegistro() { return Optional.ofNullable(numeroRegistro); }
    public Optional<String> getMotivoRecusa() { return Optional.ofNullable(motivoRecusa); }
    public Optional<MotivoPendencia> getMotivoPendencia() { return Optional.ofNullable(motivoPendencia); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
}
