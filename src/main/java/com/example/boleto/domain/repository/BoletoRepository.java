package com.example.boleto.domain.repository;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.exception.BoletoNaoEncontradoException;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Documento;
import com.example.boleto.shared.utils.Pagina;

import java.util.Optional;

/** Porta de saída para persistência. O domínio define o contrato; {@code adapter.database} implementa. */
public interface BoletoRepository {

    void salvar(Boleto boleto);

    Optional<Boleto> buscarPorId(BoletoId id);

    Pagina<Boleto> listar(FiltroBoleto filtro, int pagina, int tamanho);

    long contarEmAbertoPorPagador(Documento documento);

    void remover(BoletoId id);

    /** Atalho usado por todos os use cases que operam sobre um boleto existente. */
    default Boleto obter(BoletoId id) {
        return buscarPorId(id).orElseThrow(() -> new BoletoNaoEncontradoException(id));
    }
}
