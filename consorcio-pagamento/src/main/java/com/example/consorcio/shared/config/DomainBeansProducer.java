package com.example.consorcio.shared.config;

import com.example.consorcio.domain.factory.ProcessoPagamentoFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;

/**
 * Liga objetos de domínio (Java puro) ao CDI, para o domínio não precisar de anotações de framework.
 */
@ApplicationScoped
public class DomainBeansProducer {

    @Produces
    @ApplicationScoped
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Produces
    @ApplicationScoped
    ProcessoPagamentoFactory processoPagamentoFactory(
            Clock clock,
            @ConfigProperty(name = "consorcio.pagamento.prazo-maximo-vencimento-dias") int prazoMaximo) {
        return new ProcessoPagamentoFactory(clock, prazoMaximo);
    }
}
