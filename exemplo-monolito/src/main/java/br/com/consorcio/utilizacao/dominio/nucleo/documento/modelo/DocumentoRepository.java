package br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentoRepository implements PanacheRepository<Documento> {
}
