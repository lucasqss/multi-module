package com.example.processo.integracoes.pdf.client;

import com.example.processo.integracoes.pdf.dto.Documento;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * Simulação do sistema de PDF para o perfil dev: só registra o pedido no log, sem rede. Mesmo mecanismo do
 * {@code CadastroClientSimulado}: existe só no build de dev e tem prioridade sobre o REST Client gerado.
 */
@IfBuildProfile("dev")
@Alternative
@Priority(1)
@RestClient
@ApplicationScoped
public class SistemaDePdfSimulado implements SistemaDePdf {

    private static final Logger LOG = Logger.getLogger(SistemaDePdfSimulado.class);

    @Override
    public void gerar(Documento documento) {
        LOG.infof("PDF simulado: %s do processo %s", documento.modelo(), documento.referencia());
    }
}
