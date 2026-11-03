package com.example.boleto.adapter;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.exception.AlteracaoConcorrenteException;
import com.example.boleto.domain.specification.boleto.SolicitacaoBoleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.DadosBoleto;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagador;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lock otimista: duas transações leem o mesmo boleto; a segunda a gravar recebe
 * {@link AlteracaoConcorrenteException} (409) em vez de sobrescrever a primeira em silêncio.
 */
@QuarkusTest
class ConcorrenciaTest {

    @Inject
    BoletoRepository boletos;

    @Inject
    Clock clock;

    @Test
    void segundaGravacaoSobreVersaoAntigaFalha() {
        BoletoId id = QuarkusTransaction.requiringNew().call(() -> {
            var vencimento = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            var dados = DadosBoleto.de(Pagador.de("Concorrência", "99999999999", "c@example.com"),
                    new BigDecimal("100.00"), vencimento);
            Boleto novo = Boleto.emitir(SolicitacaoBoleto.paraEmissao(dados, clock, 60, 0));
            boletos.salvar(novo);
            return novo.getId();
        });

        assertThrows(AlteracaoConcorrenteException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            var leituraAntiga = boletos.obter(id); // transação A lê (versão 0)

            QuarkusTransaction.requiringNew().run(() -> { // transação B altera e confirma (versão 1)
                var outro = boletos.obter(id);
                outro.cancelar(new Motivo("cancelado por B"));
                boletos.salvar(outro);
            });

            leituraAntiga.cancelar(new Motivo("cancelado por A")); // A tenta gravar sobre a versão 0
            boletos.salvar(leituraAntiga);
        }));
    }
}
