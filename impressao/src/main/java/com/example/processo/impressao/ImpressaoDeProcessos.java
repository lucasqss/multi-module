package com.example.processo.impressao;

import com.example.processo.core.motor.domain.ProcessoCriado;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * Pede o PDF de abertura de cada processo criado, de qualquer segmento. Roda só depois do commit (AFTER_SUCCESS):
 * processo que não foi gravado não é impresso, e falha na impressão não desfaz o processo — fica no log.
 */
@ApplicationScoped
public class ImpressaoDeProcessos {

    private static final Logger LOG = Logger.getLogger(ImpressaoDeProcessos.class);

    private final SistemaDePdf sistemaDePdf;

    ImpressaoDeProcessos(@RestClient SistemaDePdf sistemaDePdf) {
        this.sistemaDePdf = sistemaDePdf;
    }

    void aoCriar(@Observes(during = TransactionPhase.AFTER_SUCCESS) ProcessoCriado evento) {
        var documento = Documento.deAbertura(evento.processo());
        try {
            sistemaDePdf.gerar(documento);
        } catch (RuntimeException e) {
            LOG.errorf(e, "Falha ao pedir o PDF %s do processo %s", documento.modelo(), documento.referencia());
        }
    }
}
