package com.example.biblioteca.query;

import com.example.jpacore.AutoriaEntity;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Somente leitura: só existem os métodos declarados aqui (nada de persist/delete herdados).
 * As consultas partem do lado @ManyToOne (AutoriaEntity → livro) para que o HQL seja validado na
 * compilação; joins que percorrem coleções desligam essa validação (ver docs/design.md).
 */
@Repository
public interface AcervoQueries {

    /** Uma linha por autor, já com o livro; filtros opcionais por título e por qualquer um dos autores. */
    @Query("""
            select a from AutoriaEntity a
            join fetch a.livro l
            where (:titulo is null or lower(l.titulo) like lower('%' || :titulo || '%'))
              and (:autor is null or exists (
                    select 1 from AutoriaEntity x
                    where x.livroId = l.id and lower(x.nome) like lower('%' || :autor || '%')))
            order by l.titulo, l.id, a.ordem""")
    List<AutoriaEntity> autorias(String titulo, String autor);

    @Query("""
            select a from AutoriaEntity a
            join fetch a.livro
            where a.livroId in :livroIds
            order by a.livroId, a.ordem""")
    List<AutoriaEntity> autoriasDos(Collection<UUID> livroIds);

    @Query("""
            select new com.example.biblioteca.query.Disponiveis(e.livroId, count(*))
            from ExemplarEntity e
            where e.situacao = DISPONIVEL and e.livroId in :livroIds
            group by e.livroId""")
    List<Disponiveis> disponiveisDos(Collection<UUID> livroIds);
}
