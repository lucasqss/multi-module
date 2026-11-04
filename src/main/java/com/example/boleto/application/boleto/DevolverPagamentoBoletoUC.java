package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.adapter.pagamentos.SistemaPagamentos;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Motivo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * O financeiro decide devolver o pagamento divergente. EM_ANALISE -> DEVOLVIDO, e o sistema externo
 * de pagamentos é acionado para estornar o dinheiro.
 *
 * <p>Mesma ordem de toda integração de saída: domínio decide → salva (detecta concorrência) → só então
 * chama o sistema externo. Risco restante ("pediu a devolução, mas o commit falhou"): padrão Outbox.
 */
@ApplicationScoped
public class DevolverPagamentoBoletoUC {

    private final BoletoRepository repository;
    private final SistemaPagamentos sistemaPagamentos;

    public DevolverPagamentoBoletoUC(BoletoRepository repository, SistemaPagamentos sistemaPagamentos) {
        this.repository = repository;
        this.sistemaPagamentos = sistemaPagamentos;
    }

    @Transactional
    public BoletoOutput executar(UUID id, String motivo) {
        var boleto = repository.obter(new BoletoId(id));
        boleto.devolverPagamento(new Motivo(motivo));
        repository.salvar(boleto);

        sistemaPagamentos.solicitarDevolucao(boleto);
        return BoletoOutput.de(boleto);
    }
}
