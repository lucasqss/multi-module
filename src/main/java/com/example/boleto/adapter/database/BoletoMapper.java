package com.example.boleto.adapter.database;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.DadosBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.LinhaDigitavel;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagador;
import com.example.boleto.domain.valueobject.Pagamento;

/** Converte domínio <-> JPA. Único lugar que conhece os dois lados. */
final class BoletoMapper {

    private BoletoMapper() {
    }

    /** Copia o domínio para dentro de uma entidade JPA (nova ou já gerenciada). */
    static void copiar(Boleto origem, BoletoJpaEntity destino) {
        destino.id = origem.getId().valor();
        destino.pagadorNome = origem.getPagador().nome();
        destino.pagadorDocumento = origem.getPagador().documento().numero();
        destino.pagadorEmail = origem.getPagador().email();
        destino.valor = origem.getValor().valor();
        destino.dataVencimento = origem.getDataVencimento();
        destino.linhaDigitavel = origem.getLinhaDigitavel().valor();
        destino.status = origem.getStatus();
        destino.emitidoEm = origem.getEmitidoEm();
        destino.notificadoEm = origem.getNotificadoEm().orElse(null);
        destino.valorPago = origem.getPagamento().map(p -> p.valorPago().valor()).orElse(null);
        destino.dataPagamento = origem.getPagamento().map(Pagamento::data).orElse(null);
        destino.motivoCancelamento = origem.getMotivoCancelamento().map(Motivo::texto).orElse(null);
    }

    static Boleto paraDominio(BoletoJpaEntity e) {
        var pagador = Pagador.de(e.pagadorNome, e.pagadorDocumento, e.pagadorEmail);
        var pagamento = e.valorPago == null ? null : new Pagamento(new Dinheiro(e.valorPago), e.dataPagamento);
        return Boleto.reconstituir(
                new BoletoId(e.id),
                e.emitidoEm,
                new DadosBoleto(pagador, new Dinheiro(e.valor), e.dataVencimento),
                new LinhaDigitavel(e.linhaDigitavel),
                e.status,
                e.notificadoEm,
                pagamento,
                e.motivoCancelamento == null ? null : new Motivo(e.motivoCancelamento));
    }
}
