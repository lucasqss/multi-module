package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * DELETE (exclusão física). Só é permitida no rascunho (EM_EDICAO), que ninguém de fora conhece;
 * depois de emitido, o caminho é CANCELAR, que preserva o histórico. Quem decide é o estado.
 */
@ApplicationScoped
public class ExcluirBoletoUC {

    private final BoletoRepository repository;

    public ExcluirBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void executar(UUID id) {
        var boleto = repository.obter(new BoletoId(id));
        boleto.validarExclusao();
        repository.remover(boleto.getId());
    }
}
