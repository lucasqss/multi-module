package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrdemRepository implements PanacheRepository<OrdemPagamento> {
}
