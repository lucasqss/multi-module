package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.exception.BoletoCanceladoNoEmissorException;
import com.example.pagamento.domain.exception.ProcessoPagamentoNaoEncontradoException;
import com.example.pagamento.domain.specification.processo.AprovacaoProcesso;
import com.example.pagamento.domain.specification.processo.RegrasProcessoPagamento;
import com.example.pagamento.domain.specification.processo.RetornoPagamento;
import com.example.pagamento.domain.specification.processo.SubmissaoProcesso;
import com.example.pagamento.domain.state.EstadoProcesso;
import com.example.pagamento.domain.state.StatusProcesso;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Matricula;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import com.example.pagamento.domain.valueobject.RetornoBanco;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root do processo de pagamento de boletos.
 *
 * <p>O BOLETO não é nosso (pertence ao sistema emissor): guardamos só uma cópia dos dados em
 * {@link BoletosDoProcesso}. O que é nosso é o PROCESSO: escolher os boletos, aprovar, enviar ao banco
 * e acompanhar o resultado.
 *
 * <p>Partes do agregado (nenhuma tem repositório ou use case próprio; tudo passa por aqui):
 * <ul>
 *   <li>{@link BoletosDoProcesso}, {@link ContaDebito}: value objects, substituídos inteiros;</li>
 *   <li>{@link Aprovacao}, {@link TentativaPagamento}: ENTIDADES internas, com identidade dentro do processo.</li>
 * </ul>
 *
 * <p>Padrão das operações: 1º o ESTADO diz se a ação é permitida (409); 2º as SPECIFICATIONS dizem se
 * os dados são válidos (422) ou, para fatos externos, classificam o fato; 3º só então o estado muda.
 */
public class ProcessoPagamento {

    private final ProcessoPagamentoId id;
    private final Matricula solicitante;
    private final Instant criadoEm;
    private ContaDebito contaDebito;
    private LocalDate dataPagamento;
    private BoletosDoProcesso boletos;
    private final List<Aprovacao> aprovacoes;
    private final List<TentativaPagamento> tentativas;
    private EstadoProcesso estado;
    private Motivo motivoRejeicao;
    private Motivo motivoCancelamento;
    private MotivoAnalise motivoAnalise;

    private ProcessoPagamento(ProcessoPagamentoId id, Matricula solicitante, Instant criadoEm, ContaDebito contaDebito,
                              LocalDate dataPagamento, BoletosDoProcesso boletos, List<Aprovacao> aprovacoes,
                              List<TentativaPagamento> tentativas, EstadoProcesso estado, Motivo motivoRejeicao,
                              Motivo motivoCancelamento, MotivoAnalise motivoAnalise) {
        this.id = Objects.requireNonNull(id);
        this.solicitante = Objects.requireNonNull(solicitante);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.contaDebito = contaDebito;
        this.dataPagamento = dataPagamento;
        this.boletos = Objects.requireNonNull(boletos);
        this.aprovacoes = new ArrayList<>(aprovacoes);
        this.tentativas = new ArrayList<>(tentativas);
        this.estado = Objects.requireNonNull(estado);
        this.motivoRejeicao = motivoRejeicao;
        this.motivoCancelamento = motivoCancelamento;
        this.motivoAnalise = motivoAnalise;
    }

    // =============================== criação ===============================

    /** Cria o rascunho (EM_EDICAO) só com o solicitante. Completude é exigida em {@link #submeter}. */
    public static ProcessoPagamento criar(Matricula solicitante, Instant agora) {
        return new ProcessoPagamento(ProcessoPagamentoId.novo(), solicitante, agora, null, null,
                BoletosDoProcesso.NENHUM, List.of(), List.of(), EstadoProcesso.inicial(), null, null, null);
    }

    /** Reconstrói a partir da persistência, sem reaplicar regras (o dado já foi validado). */
    public static ProcessoPagamento reconstituir(ProcessoPagamentoId id, Matricula solicitante, Instant criadoEm,
                                                 ContaDebito contaDebito, LocalDate dataPagamento,
                                                 BoletosDoProcesso boletos, List<Aprovacao> aprovacoes,
                                                 List<TentativaPagamento> tentativas, StatusProcesso status,
                                                 Motivo motivoRejeicao, Motivo motivoCancelamento,
                                                 MotivoAnalise motivoAnalise) {
        return new ProcessoPagamento(id, solicitante, criadoEm, contaDebito, dataPagamento, boletos, aprovacoes,
                tentativas, EstadoProcesso.de(status), motivoRejeicao, motivoCancelamento, motivoAnalise);
    }

    // =============================== seções (só em EM_EDICAO) ===============================

    /** Seção "dados gerais": conta e data. Regras que dependem de "hoje" valem na submissão. */
    public void alterarDadosGerais(ContaDebito novaConta, LocalDate novaData) {
        estado.validarEdicao();
        this.contaDebito = novaConta;
        this.dataPagamento = novaData;
    }

    /** Seção "boletos": substitui inteira. Regras da seção agora; completude só na submissão. */
    public void salvarBoletos(BoletosDoProcesso novos) {
        estado.validarEdicao();
        RegrasProcessoPagamento.BOLETOS.validar(novos);
        this.boletos = novos;
    }

    /** Só verifica: quem remove de fato é o repositório. */
    public void validarExclusao() {
        estado.validarExclusao();
    }

    // =============================== fluxo de aprovação ===============================

    /** Finaliza a edição: exige completude. EM_EDICAO -> AGUARDANDO_APROVACAO. */
    public void submeter(LocalDate hoje) {
        var proximo = estado.submeter();
        RegrasProcessoPagamento.SUBMISSAO.validar(new SubmissaoProcesso(boletos, contaDebito, dataPagamento, hoje));
        estado = proximo;
    }

    /** Registra uma aprovação. A ALÇADA decide se já basta (APROVADO) ou se falta outra (continua aguardando). */
    public void aprovar(Matricula aprovador, Instant agora) {
        var candidato = new AprovacaoProcesso(solicitante, aprovadores(), aprovador, getValorTotal());
        var proximo = estado.registrarAprovacao(RegrasProcessoPagamento.ALCADA_ATINGIDA.isSatisfiedBy(candidato));
        RegrasProcessoPagamento.APROVACAO.validar(candidato);
        aprovacoes.add(Aprovacao.de(aprovador, agora));
        estado = proximo;
    }

    public void rejeitar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.rejeitar();
        motivoRejeicao = motivo;
    }

    /** Volta para edição; as aprovações anteriores deixam de valer. */
    public void reabrir() {
        estado = estado.reabrir();
        aprovacoes.clear();
        motivoRejeicao = null;
    }

    // =============================== pagamento no banco (sistema externo) ===============================

    /** APROVADO -> EM_PROCESSAMENTO, com uma nova tentativa. Quem chama o banco é o use case, depois de salvar. */
    public void enviarParaPagamento(Instant agora) {
        estado = estado.enviarParaPagamento();
        novaTentativa(agora);
    }

    /** FALHOU -> EM_PROCESSAMENTO, com uma nova tentativa (a anterior fica no histórico). */
    public void retentarPagamento(Instant agora) {
        estado = estado.retentarPagamento();
        novaTentativa(agora);
    }

    /**
     * Retorno do banco para uma tentativa. É um FATO: a conformidade CLASSIFICA (não lança).
     * Idempotente: retorno repetido de uma tentativa já concluída não muda nada.
     */
    public void registrarRetornoBanco(RetornoBanco retorno) {
        var tentativa = tentativa(retorno.idTransacao());
        if (!tentativa.aberta()) {
            return; // retorno repetido: já processado
        }
        var divergencia = RegrasProcessoPagamento.CONFORMIDADE_RETORNO
                .primeiraViolacao(new RetornoPagamento(getValorTotal(), retorno));

        var proximo = estado.receberRetornoBanco(retorno.liquidado(), divergencia.isEmpty());
        tentativa.concluir(retorno);
        estado = proximo;
        divergencia.map(MotivoAnalise::de).ifPresent(m -> motivoAnalise = m);
    }

    // =============================== fatos do sistema emissor ===============================

    /** O emissor cancelou um boleto deste processo. Antes do envio: cancela. Depois: análise. */
    public void registrarBoletoCanceladoNoEmissor(String idBoletoNoEmissor) {
        if (!boletos.contem(idBoletoNoEmissor)) {
            throw new IllegalArgumentException("Boleto %s não pertence ao processo %s".formatted(idBoletoNoEmissor, id));
        }
        var anterior = estado;
        estado = estado.boletoCanceladoNoEmissor();
        if (estado == anterior) {
            return; // já cancelado ou já em análise: nada novo a registrar
        }
        var fato = new BoletoCanceladoNoEmissorException(idBoletoNoEmissor);
        if (estado.emAnalise()) {
            motivoAnalise = MotivoAnalise.de(fato);
        } else {
            motivoCancelamento = new Motivo("Boleto %s cancelado pelo emissor".formatted(idBoletoNoEmissor));
        }
    }

    public void cancelar(Motivo motivo) {
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        estado = estado.cancelar();
        motivoCancelamento = motivo;
    }

    // =============================== internos ===============================

    private void novaTentativa(Instant agora) {
        tentativas.add(TentativaPagamento.nova(id, tentativas.size() + 1, agora));
    }

    private TentativaPagamento tentativa(String idTransacao) {
        return tentativas.stream().filter(t -> t.getIdTransacao().equals(idTransacao)).findFirst()
                .orElseThrow(() -> new ProcessoPagamentoNaoEncontradoException(idTransacao));
    }

    private List<Matricula> aprovadores() {
        return aprovacoes.stream().map(Aprovacao::getAprovador).toList();
    }

    // =============================== leitura ===============================

    public ProcessoPagamentoId getId() { return id; }
    public Matricula getSolicitante() { return solicitante; }
    public Instant getCriadoEm() { return criadoEm; }
    public Optional<ContaDebito> getContaDebito() { return Optional.ofNullable(contaDebito); }
    public Optional<LocalDate> getDataPagamento() { return Optional.ofNullable(dataPagamento); }
    public BoletosDoProcesso getBoletos() { return boletos; }
    /** Valor do processo = soma dos boletos. */
    public Dinheiro getValorTotal() { return boletos.total(); }
    public List<Aprovacao> getAprovacoes() { return List.copyOf(aprovacoes); }
    public List<TentativaPagamento> getTentativas() { return List.copyOf(tentativas); }
    /** A tentativa mais recente (a que o use case envia ao banco). */
    public Optional<TentativaPagamento> getTentativaAtual() {
        return tentativas.isEmpty() ? Optional.empty() : Optional.of(tentativas.getLast());
    }
    public StatusProcesso getStatus() { return estado.status(); }
    public Optional<Motivo> getMotivoRejeicao() { return Optional.ofNullable(motivoRejeicao); }
    public Optional<Motivo> getMotivoCancelamento() { return Optional.ofNullable(motivoCancelamento); }
    public Optional<MotivoAnalise> getMotivoAnalise() { return Optional.ofNullable(motivoAnalise); }
}
