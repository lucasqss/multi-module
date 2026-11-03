package com.example.boleto.application.boleto;

import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.ReceberPagamentoInput;
import com.example.boleto.domain.repository.BoletoRepository;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Simula o RECEBIMENTO do pagamento informado pelo banco. EMITIDO|NOTIFICADO -> PAGO.
 * O estado diz se o boleto ainda aceita pagamento (409); as specifications de recebimento
 * dizem se o pagamento informado é válido (422).
 */
@ApplicationScoped
public class ReceberPagamentoBoletoUC {

    private final BoletoRepository repository;
    private final Clock clock;

    public ReceberPagamentoBoletoUC(BoletoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public BoletoOutput executar(ReceberPagamentoInput input) {
        var boleto = repository.obter(new BoletoId(input.id()));
        boleto.receberPagamento(input.pagamento(), LocalDate.now(clock));
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
