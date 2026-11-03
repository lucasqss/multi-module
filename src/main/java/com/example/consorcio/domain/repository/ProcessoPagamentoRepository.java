package com.example.consorcio.domain.repository;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.exception.ProcessoPagamentoNaoEncontradoException;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import com.example.consorcio.shared.utils.Pagina;

import java.util.Optional;

/** Porta de saída. O domínio define o contrato; {@code adapter.database} implementa. */
public interface ProcessoPagamentoRepository {

    void salvar(ProcessoPagamento processo);

    Optional<ProcessoPagamento> buscarPorId(ProcessoPagamentoId id);

    Pagina<ProcessoPagamento> listar(FiltroProcessoPagamento filtro, int pagina, int tamanho);

    boolean existeProcessoEmAndamentoParaCota(IdentificadorCota cota);

    void remover(ProcessoPagamentoId id);

    /** Atalho usado por todos os use cases que operam sobre um processo existente. */
    default ProcessoPagamento obter(ProcessoPagamentoId id) {
        return buscarPorId(id).orElseThrow(() -> new ProcessoPagamentoNaoEncontradoException(id));
    }
}
