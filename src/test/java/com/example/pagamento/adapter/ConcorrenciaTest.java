package com.example.pagamento.adapter;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.exception.AlteracaoConcorrenteException;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.LinhaDigitavel;
import com.example.pagamento.domain.valueobject.Matricula;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lock otimista no agregado inteiro: duas transações leem o mesmo processo e editam PARTES diferentes
 * (boletos × dados gerais); a segunda a gravar recebe {@link AlteracaoConcorrenteException} (409).
 */
@QuarkusTest
class ConcorrenciaTest {

    @Inject
    ProcessoPagamentoRepository processos;

    @Inject
    Clock clock;

    @Test
    void edicoesConcorrentesDePartesDiferentesNaoSeSobrescrevem() {
        ProcessoPagamentoId id = QuarkusTransaction.requiringNew().call(() -> {
            ProcessoPagamento novo = ProcessoPagamento.criar(new Matricula("F9999999"), clock.instant());
            processos.salvar(novo);
            return novo.getId();
        });

        assertThrows(AlteracaoConcorrenteException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            var leituraAntiga = processos.obter(id); // transação A lê (versão 0)

            QuarkusTransaction.requiringNew().run(() -> { // transação B salva os BOLETOS (versão 1)
                var outro = processos.obter(id);
                var linha = new LinhaDigitavel("9001" + "0".repeat(24) + "1" + "20991231" + "0000010000");
                outro.salvarBoletos(new BoletosDoProcesso(List.of(
                        new BoletoAPagar("EMI-1", linha, Dinheiro.de("100.00"), LocalDate.of(2099, 12, 31), "Fornecedor"))));
                processos.salvar(outro);
            });

            // A salva OUTRA parte sobre a versão 0: sem o lock, apagaria os boletos gravados por B
            leituraAntiga.alterarDadosGerais(new ContaDebito("1234", "1"), null);
            processos.salvar(leituraAntiga);
        }));
    }
}
