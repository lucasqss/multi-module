package com.example.consorcio.adapter.database;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

final class ProcessoPagamentoMapper {

    private ProcessoPagamentoMapper() {
    }

    static ProcessoPagamentoJpaEntity paraJpa(ProcessoPagamento processo, ProcessoPagamentoJpaEntity destino) {
        destino.id = processo.getId().valor();
        destino.grupo = processo.getCota().grupo();
        destino.cota = processo.getCota().cota();
        destino.valor = processo.getValor().valor();
        destino.formaPagamento = processo.getFormaPagamento();
        destino.agenciaDebito = processo.getContaDebito().map(ContaDebito::agencia).orElse(null);
        destino.contaDebito = processo.getContaDebito().map(ContaDebito::conta).orElse(null);
        destino.dataVencimento = processo.getDataVencimento();
        destino.status = processo.getStatus();
        destino.motivoCancelamento = processo.getMotivoCancelamento().orElse(null);
        destino.criadoEm = processo.getCriadoEm();
        return destino;
    }

    static ProcessoPagamento paraDominio(ProcessoPagamentoJpaEntity e) {
        return ProcessoPagamento.reconstituir(
                new ProcessoPagamentoId(e.id),
                new IdentificadorCota(e.grupo, e.cota),
                new Dinheiro(e.valor),
                e.formaPagamento,
                e.agenciaDebito == null ? null : new ContaDebito(e.agenciaDebito, e.contaDebito),
                e.dataVencimento,
                e.criadoEm,
                e.status,
                e.motivoCancelamento);
    }
}
