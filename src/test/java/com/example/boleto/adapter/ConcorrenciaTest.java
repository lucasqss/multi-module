package com.example.boleto.adapter;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.exception.AlteracaoConcorrenteException;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Email;
import com.example.boleto.domain.valueobject.ItemBoleto;
import com.example.boleto.domain.valueobject.ItensBoleto;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lock otimista no agregado inteiro: duas transações leem o mesmo boleto e editam seções
 * diferentes; a segunda a gravar recebe
 * {@link AlteracaoConcorrenteException} (409) em vez de sobrescrever a primeira em silêncio.
 */
@QuarkusTest
class ConcorrenciaTest {

    @Inject
    BoletoRepository boletos;

    @Inject
    Clock clock;

    @Test
    void edicoesConcorrentesDeSecoesDiferentesNaoSeSobrescrevem() {
        BoletoId id = QuarkusTransaction.requiringNew().call(() -> {
            var vencimento = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            Boleto novo = Boleto.criar(Pagador.de("Concorrência", "99999999999", "c@example.com"), vencimento, clock.instant());
            boletos.salvar(novo);
            return novo.getId();
        });

        assertThrows(AlteracaoConcorrenteException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            var leituraAntiga = boletos.obter(id); // transação A lê (versão 0)

            QuarkusTransaction.requiringNew().run(() -> { // transação B salva os ITENS (versão 1)
                var outro = boletos.obter(id);
                outro.salvarItens(new ItensBoleto(List.of(ItemBoleto.de("Item B", new BigDecimal("100.00")))));
                boletos.salvar(outro);
            });

            // A salva OUTRA seção sobre a versão 0: sem o lock, apagaria os itens gravados por B
            leituraAntiga.salvarDestinatarios(new Destinatarios(List.of(new Email("a@example.com"))));
            boletos.salvar(leituraAntiga);
        }));
    }
}
