package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.SalvarDestinatariosBoletoInput;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Salva a SEÇÃO (opcional) de destinatários extras da notificação, inteira (PUT). Mesmo padrão de
 * {@link SalvarItensBoletoUC}: um caso de uso por seção, a seção é parte do boleto.
 */
@ApplicationScoped
public class SalvarDestinatariosBoletoUC {

    private final BoletoRepository repository;

    public SalvarDestinatariosBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(SalvarDestinatariosBoletoInput input) {
        var boleto = repository.obter(new BoletoId(input.id()));
        boleto.salvarDestinatarios(input.destinatarios());
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
