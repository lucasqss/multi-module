package com.example.boleto.adapter.database;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Email;
import com.example.boleto.domain.valueobject.ItemBoleto;
import com.example.boleto.domain.valueobject.ItensBoleto;
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
        destino.pagadorEmail = origem.getPagador().email().valor();
        destino.dataVencimento = origem.getDataVencimento();
        // coleções gerenciadas: limpar e repovoar (não trocar a instância da lista)
        destino.itens.clear();
        origem.getItens().itens().forEach(i ->
                destino.itens.add(new BoletoJpaEntity.ItemJpa(i.descricao(), i.valor().valor())));
        destino.destinatarios.clear();
        origem.getDestinatarios().emails().forEach(e -> destino.destinatarios.add(e.valor()));
        destino.status = origem.getStatus();
        destino.linhaDigitavel = origem.getLinhaDigitavel().map(LinhaDigitavel::valor).orElse(null);
        destino.criadoEm = origem.getCriadoEm();
        destino.emitidoEm = origem.getEmitidoEm().orElse(null);
        destino.notificadoEm = origem.getNotificadoEm().orElse(null);
        destino.valorPago = origem.getPagamento().map(p -> p.valorPago().valor()).orElse(null);
        destino.dataPagamento = origem.getPagamento().map(Pagamento::data).orElse(null);
        destino.motivoCancelamento = origem.getMotivoCancelamento().map(Motivo::texto).orElse(null);
    }

    static Boleto paraDominio(BoletoJpaEntity e) {
        var itens = new ItensBoleto(e.itens.stream()
                .map(i -> new ItemBoleto(i.descricao, new Dinheiro(i.valor))).toList());
        var destinatarios = new Destinatarios(e.destinatarios.stream().map(Email::new).toList());
        var pagamento = e.valorPago == null ? null : new Pagamento(new Dinheiro(e.valorPago), e.dataPagamento);
        return Boleto.reconstituir(
                new BoletoId(e.id),
                Pagador.de(e.pagadorNome, e.pagadorDocumento, e.pagadorEmail),
                e.criadoEm,
                e.dataVencimento,
                itens,
                destinatarios,
                e.status,
                e.linhaDigitavel == null ? null : new LinhaDigitavel(e.linhaDigitavel),
                e.emitidoEm,
                e.notificadoEm,
                pagamento,
                e.motivoCancelamento == null ? null : new Motivo(e.motivoCancelamento));
    }
}
