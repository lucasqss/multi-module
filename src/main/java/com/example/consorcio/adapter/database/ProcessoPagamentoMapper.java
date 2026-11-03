package com.example.consorcio.adapter.database;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.DadosPagamento;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.Motivo;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

/** Converte domínio <-> JPA. Único lugar que conhece os dois lados. */
final class ProcessoPagamentoMapper {

    private ProcessoPagamentoMapper() {
    }

    /** Copia o domínio para dentro de uma entidade JPA (nova ou já gerenciada). */
    static void copiar(ProcessoPagamento origem, ProcessoPagamentoJpaEntity destino) {
        destino.id = origem.getId().valor();
        destino.grupo = origem.getCota().grupo();
        destino.cota = origem.getCota().cota();
        destino.valor = origem.getValor().valor();
        destino.formaPagamento = origem.getFormaPagamento();
        destino.agenciaDebito = origem.getContaDebito().map(ContaDebito::agencia).orElse(null);
        destino.contaDebito = origem.getContaDebito().map(ContaDebito::conta).orElse(null);
        destino.dataVencimento = origem.getDataVencimento();
        destino.status = origem.getStatus();
        destino.motivoRejeicao = origem.getMotivoRejeicao().map(Motivo::texto).orElse(null);
        destino.motivoCancelamento = origem.getMotivoCancelamento().map(Motivo::texto).orElse(null);
        destino.criadoEm = origem.getCriadoEm();
    }

    static ProcessoPagamento paraDominio(ProcessoPagamentoJpaEntity e) {
        var conta = e.agenciaDebito == null ? null : new ContaDebito(e.agenciaDebito, e.contaDebito);
        return ProcessoPagamento.reconstituir(
                new ProcessoPagamentoId(e.id),
                new IdentificadorCota(e.grupo, e.cota),
                e.criadoEm,
                new DadosPagamento(new Dinheiro(e.valor), e.formaPagamento, e.dataVencimento, conta),
                e.status,
                e.motivoRejeicao == null ? null : new Motivo(e.motivoRejeicao),
                e.motivoCancelamento == null ? null : new Motivo(e.motivoCancelamento));
    }
}
