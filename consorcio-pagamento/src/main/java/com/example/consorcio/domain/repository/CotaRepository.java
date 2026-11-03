package com.example.consorcio.domain.repository;

import com.example.consorcio.domain.exception.RecursoNaoEncontradoException;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.valueobject.IdentificadorCota;

import java.util.Optional;

/** Porta de saída para o sistema que é dono das cotas (implementada em {@code adapter.cota}). */
public interface CotaRepository {

    Optional<Cota> buscar(IdentificadorCota identificador);

    default Cota obter(IdentificadorCota identificador) {
        return buscar(identificador).orElseThrow(() -> new RecursoNaoEncontradoException("Cota", identificador));
    }
}
