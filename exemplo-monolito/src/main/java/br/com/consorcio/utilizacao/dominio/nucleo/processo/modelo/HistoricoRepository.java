package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HistoricoRepository implements PanacheRepository<Historico> {
}
