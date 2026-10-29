package br.com.consorcio.utilizacao.integracao.outbox;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MensagemOutboxRepository implements PanacheRepository<MensagemOutbox> {
}
