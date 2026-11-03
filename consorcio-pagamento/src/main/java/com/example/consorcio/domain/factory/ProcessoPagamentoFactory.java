package com.example.consorcio.domain.factory;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.specification.CotaAceitaPagamentoSpec;
import com.example.consorcio.domain.specification.Specification;
import com.example.consorcio.domain.specification.ValorDentroDoSaldoDevedorSpec;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.Dinheiro;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Cria um ProcessoPagamento a partir de uma Cota, aplicando as regras que envolvem
 * mais de um objeto do domínio (specifications). Classe Java pura: sem anotação de framework;
 * quem a disponibiliza para injeção é {@code shared.config.DomainBeansProducer}.
 */
public class ProcessoPagamentoFactory {

    private final Clock clock;
    private final int prazoMaximoVencimentoDias;

    public ProcessoPagamentoFactory(Clock clock, int prazoMaximoVencimentoDias) {
        this.clock = clock;
        this.prazoMaximoVencimentoDias = prazoMaximoVencimentoDias;
    }

    public ProcessoPagamento criar(Cota cota, Dinheiro valor, FormaPagamento formaPagamento,
                                   ContaDebito contaDebito, LocalDate dataVencimento) {
        List<Specification<Cota>> regras = List.of(
                new CotaAceitaPagamentoSpec(),
                new ValorDentroDoSaldoDevedorSpec(valor));

        for (Specification<Cota> regra : regras) {
            if (!regra.isSatisfiedBy(cota)) {
                throw new RegraNegocioException(regra.codigo(), regra.mensagem(cota));
            }
        }

        return ProcessoPagamento.abrir(cota.identificador(), valor, formaPagamento, contaDebito, dataVencimento,
                clock.instant(), LocalDate.now(clock), prazoMaximoVencimentoDias);
    }
}
