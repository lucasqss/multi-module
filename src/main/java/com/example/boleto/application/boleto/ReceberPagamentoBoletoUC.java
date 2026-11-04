package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.ReceberPagamentoInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * O SISTEMA EXTERNO de pagamentos avisa que um boleto foi pago (chamado pelo webhook, não pelo usuário).
 *
 * <p>O aviso é um FATO: o domínio não o recusa por regra de negócio, só o classifica (PAGO ou
 * EM_ANALISE, com o motivo). Avisos repetidos (mesma transação) são ignorados pela entidade,
 * porque webhooks são reenviados quando a resposta não chega.
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
        var boleto = repository.obterPorLinhaDigitavel(input.linha());
        boleto.registrarPagamento(input.pagamento(clock.getZone()), LocalDate.now(clock));
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
