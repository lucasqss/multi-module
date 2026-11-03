package com.example.consorcio.domain.repository;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

import java.util.Optional;

/** Porta de saída. O domínio define o contrato; o adapter.database implementa. */
public interface ProcessoPagamentoRepository {

    void salvar(ProcessoPagamento processo);

    Optional<ProcessoPagamento> buscarPorId(ProcessoPagamentoId id);

    boolean existeProcessoEmAbertoParaCota(IdentificadorCota cota);
}
