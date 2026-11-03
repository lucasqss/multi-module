package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.CriarBoletoInput;
import com.example.boleto.domain.entity.Boleto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;

/**
 * CREATE: cria o boleto em EM_EDICAO (rascunho) só com pagador e vencimento.
 * As demais seções são salvas por casos de uso próprios; a completude é exigida em {@link EmitirBoletoUC}.
 */
@ApplicationScoped
public class CriarBoletoUC {

    private final BoletoRepository repository;
    private final Clock clock;

    public CriarBoletoUC(BoletoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public BoletoOutput executar(CriarBoletoInput input) {
        var boleto = Boleto.criar(input.pagador(), input.dataVencimento(), clock.instant());
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
