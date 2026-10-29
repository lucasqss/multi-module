package br.com.consorcio.utilizacao.integracao.outbox;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Lê mensagens pendentes e envia, uma transação por mensagem. Enviar e marcar não são atômicos
 * entre sistemas: se cair entre os dois, a mensagem sai de novo, e a chave de idempotência protege.
 */
@ApplicationScoped
public class ProcessadorOutbox {

    private static final Logger LOG = Logger.getLogger(ProcessadorOutbox.class);

    @Inject
    MensagemOutboxRepository mensagens;

    @Inject
    Instance<Despachante> despachantes;

    @ConfigProperty(name = "outbox.max-tentativas")
    int maxTentativas;

    @Scheduled(every = "${outbox.intervalo}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void agendado() {
        processarPendentes();
    }

    /** Público para os testes dispararem o envio de forma determinística. */
    public int processarPendentes() {
        List<Long> ids = QuarkusTransaction.requiringNew().call(() ->
                mensagens.list("situacao = ?1 order by id", MensagemOutbox.Situacao.PENDENTE).stream().map(MensagemOutbox::id).toList());
        int enviadas = 0;
        for (Long id : ids) {
            if (processar(id)) {
                enviadas++;
            }
        }
        return enviadas;
    }

    private boolean processar(Long id) {
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                MensagemOutbox m = mensagens.findById(id);
                try {
                    despachante(m.tipo()).enviar(m);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                m.marcarEnviada();
            });
            return true;
        } catch (RuntimeException e) {
            LOG.warnf(e, "Falha ao enviar mensagem %d da outbox", id);
            QuarkusTransaction.requiringNew().run(() -> mensagens.findById(id).registrarFalha(String.valueOf(e.getMessage()), maxTentativas));
            return false;
        }
    }

    private Despachante despachante(String tipo) {
        return despachantes.stream().filter(d -> d.tipo().equals(tipo)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Sem despachante para " + tipo));
    }
}
