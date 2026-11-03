package com.example.consorcio.adapter;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.exception.AlteracaoConcorrenteException;
import com.example.consorcio.domain.factory.SolicitacaoPagamentoFactory;
import com.example.consorcio.domain.repository.CotaRepository;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.DadosPagamento;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.Motivo;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lock otimista: duas transações leem o mesmo processo; a segunda a gravar recebe
 * {@link AlteracaoConcorrenteException} (409) em vez de sobrescrever a primeira em silêncio.
 */
@QuarkusTest
class ConcorrenciaTest {

    @Inject
    ProcessoPagamentoRepository processos;

    @Inject
    CotaRepository cotas;

    @Inject
    SolicitacaoPagamentoFactory solicitacoes;

    @Test
    void segundaGravacaoSobreVersaoAntigaFalha() {
        ProcessoPagamentoId id = QuarkusTransaction.requiringNew().call(() -> {
            var cota = cotas.obter(new IdentificadorCota(1234, 40));
            var vencimento = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            var dados = DadosPagamento.de(new BigDecimal("100.00"), FormaPagamento.PIX, vencimento, null, null);
            ProcessoPagamento novo = ProcessoPagamento.abrir(solicitacoes.paraAbertura(cota, dados, false));
            processos.salvar(novo);
            return novo.getId();
        });

        assertThrows(AlteracaoConcorrenteException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            var leituraAntiga = processos.obter(id); // transação A lê (versão 0)

            QuarkusTransaction.requiringNew().run(() -> { // transação B altera e confirma (versão 1)
                var outro = processos.obter(id);
                outro.enviarParaProcessamento();
                processos.salvar(outro);
            });

            leituraAntiga.cancelar(new Motivo("desistência")); // A tenta gravar sobre a versão 0
            processos.salvar(leituraAntiga);
        }));
    }
}
