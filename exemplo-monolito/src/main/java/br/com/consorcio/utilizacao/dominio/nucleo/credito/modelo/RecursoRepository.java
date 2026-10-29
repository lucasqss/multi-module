package br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecursoRepository implements PanacheRepository<Recurso> {
}
