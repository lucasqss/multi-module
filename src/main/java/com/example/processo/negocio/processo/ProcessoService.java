package com.example.processo.negocio.processo;

import com.example.processo.integracao.cadastro.CadastroIntegracao;
import com.example.processo.integracao.pdf.PdfIntegracao;
import com.example.processo.modelo.Processo;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import com.example.processo.modelo.Situacao;
import com.example.processo.persistencia.repositorio.ProcessoRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import org.jboss.logging.Logger;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O motor: o mesmo fluxo de criação e o mesmo ciclo de vida para todas as modalidades. O que varia (regras, prefixo,
 * decisão do processamento) vem da {@link Modalidade}; os estados e as transições são da {@code Situacao}. É por aqui
 * que toda entrada (REST, fila) cria, processa ou cancela um processo.
 *
 * <pre>
 * criar:     regras comuns → regras da modalidade → situação cadastral (externo)  | fora de transação
 *            (com chave de idempotência já usada: devolve o processo existente, antes de tudo)
 *            → número → abrir → grava (+ chave)                                    | transação curta
 *            → pede o PDF de abertura (externo; falha só vai para o log)          | depois do commit
 * processar: carregar → ABERTO → EM_PROCESSAMENTO                                 | transação curta
 *            → modalidade avalia                                                  | fora de transação
 *            → CONCLUIDO ou RECUSADO                                              | transação curta
 * cancelar:  carregar → ABERTO → CANCELADO                                        | transação curta
 * </pre>
 *
 * As transações são curtas e explícitas ({@link QuarkusTransaction}): regra e chamada a sistema externo nunca ficam
 * dentro delas. As trocas de situação são compare-and-set: se outra requisição mudou o processo no meio, falha com
 * {@link ConflitoDeSituacaoException} em vez de sobrescrever.
 */
@ApplicationScoped
public class ProcessoService {

    private static final Logger LOG = Logger.getLogger(ProcessoService.class);

    private final List<Modalidade<?>> modalidades;
    private final ProcessoRepository repositorio;
    private final CadastroIntegracao cadastro;
    private final PdfIntegracao pdf;
    private final Clock relogio;

    ProcessoService(@Any Instance<Modalidade<?>> modalidades, ProcessoRepository repositorio,
                    CadastroIntegracao cadastro, PdfIntegracao pdf, Clock relogio) {
        this.modalidades = modalidades.stream().toList();
        this.repositorio = repositorio;
        this.cadastro = cadastro;
        this.pdf = pdf;
        this.relogio = relogio;
    }

    public <P extends Processo> P criar(P processo) {
        return criar(processo, null);
    }

    /**
     * Cria de forma idempotente: com uma {@code chave} já usada (ex.: o id de uma mensagem reentregue), devolve o
     * processo criado da primeira vez, sem reaplicar regras, gerar número nem pedir PDF. Duas criações simultâneas
     * com a mesma chave: a segunda falha ao gravar a chave, e quem tentar de novo cai neste caminho.
     */
    public <P extends Processo> P criar(P processo, String chave) {
        var modalidade = modalidadeDe(processo);
        if (chave != null) {
            var existente = repositorio.processoDaChave(chave).flatMap(repositorio::buscar);
            if (existente.isPresent()) {
                return modalidade.tipo().cast(existente.get());
            }
        }
        // A chamada ao sistema externo vem por último: só acontece se o pedido passou em todas as outras regras
        Especificacao<P> comuns = RegrasComuns.TODAS::violacao;
        comuns.e(modalidade.regras()).e(this::cadastroRegular).exigir(processo);

        QuarkusTransaction.requiringNew().run(() -> {
            processo.abrir(numero(modalidade), LocalDate.now(relogio));
            repositorio.inserir(processo);
            if (chave != null) {
                repositorio.registrarChave(chave, processo.getId());
            }
        });

        // Depois do commit: processo que não foi gravado não é impresso, e falha no PDF não desfaz o processo
        try {
            pdf.gerarAbertura(processo);
        } catch (SistemaExternoIndisponivelException e) {
            LOG.errorf(e, "Falha ao pedir o PDF de abertura do processo %s", processo.getNumero());
        }
        return processo;
    }

    public Processo processar(UUID id) {
        var processo = carregar(id);
        var de = processo.getSituacao();
        processo.iniciarProcessamento();
        gravarSituacao(processo, de);

        var avaliacao = avaliar(modalidadeDe(processo), processo);
        de = processo.getSituacao();
        if (avaliacao.aprovada()) {
            processo.concluir();
        } else {
            processo.recusar(avaliacao.motivo());
        }
        gravarSituacao(processo, de);
        return processo;
    }

    public Processo cancelar(UUID id) {
        var processo = carregar(id);
        var de = processo.getSituacao();
        processo.cancelar();
        gravarSituacao(processo, de);
        return processo;
    }

    /** Grava a nova situação só se o processo ainda estiver na anterior. */
    private void gravarSituacao(Processo processo, Situacao de) {
        boolean mudou = QuarkusTransaction.requiringNew().call(() -> repositorio.mudarSituacao(
                processo.getId(), de, processo.getSituacao(), processo.getMotivoRecusa()));
        if (!mudou) {
            throw new ConflitoDeSituacaoException(processo.getNumero());
        }
    }

    private Processo carregar(UUID id) {
        return repositorio.buscar(id).orElseThrow(() -> new ProcessoNaoEncontradoException(id));
    }

    private static <P extends Processo> Avaliacao avaliar(Modalidade<P> modalidade, Processo processo) {
        return modalidade.avaliar(modalidade.tipo().cast(processo));
    }

    private Optional<String> cadastroRegular(Processo processo) {
        return cadastro.regular(processo.getProponenteCpf())
                ? Optional.empty()
                : Optional.of("Situação cadastral irregular para o CPF " + processo.getProponenteCpf());
    }

    @SuppressWarnings("unchecked") // a modalidade foi escolhida por tipo().isInstance(processo)
    private <P extends Processo> Modalidade<P> modalidadeDe(P processo) {
        if (processo == null) {
            throw new IllegalArgumentException("Pedido sem processo");
        }
        var encontradas = modalidades.stream().filter(m -> m.tipo().isInstance(processo)).toList();
        if (encontradas.size() != 1) {
            throw new IllegalStateException(encontradas.size() + " modalidades registradas para "
                    + processo.getClass().getName() + "; deveria ser 1");
        }
        return (Modalidade<P>) encontradas.getFirst();
    }

    /** IMO-2026-000042: prefixo da modalidade, ano da abertura e o sequencial único. */
    private String numero(Modalidade<?> modalidade) {
        return "%s-%d-%06d".formatted(modalidade.prefixo(), LocalDate.now(relogio).getYear(),
                repositorio.proximoNumero());
    }
}
