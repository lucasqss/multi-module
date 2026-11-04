package com.example.pagamento.adapter.database;

import com.example.pagamento.adapter.database.PagamentoJpaEntity.BoletoJpa;
import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.LinhaDigitavel;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;
import com.example.pagamento.domain.valueobject.PagamentoId;

/** Converte domínio <-> JPA. Único lugar que conhece os dois lados. */
final class PagamentoMapper {

    private PagamentoMapper() {
    }

    /** Copia o domínio para dentro de uma entidade JPA (nova ou já gerenciada). */
    static void copiar(Pagamento origem, PagamentoJpaEntity destino) {
        destino.id = origem.getId().valor();
        destino.modalidade = origem.getModalidade();
        destino.criadoEm = origem.getCriadoEm();
        destino.boletos.clear(); // coleção gerenciada: limpar e repovoar, não trocar a lista
        origem.getBoletos().boletos().forEach(b -> destino.boletos.add(boletoJpa(b)));
        destino.agenciaDebito = origem.getContaDebito().map(ContaDebito::agencia).orElse(null);
        destino.contaDebito = origem.getContaDebito().map(ContaDebito::conta).orElse(null);
        destino.dataPagamento = origem.getDataPagamento().orElse(null);
        destino.status = origem.getStatus();
        destino.envios = origem.getEnvios();
        destino.idTransacao = origem.getIdTransacao().orElse(null);
        destino.autenticacao = origem.getAutenticacao().orElse(null);
        destino.motivoRecusa = origem.getMotivoRecusa().orElse(null);
        destino.motivoAnaliseCodigo = origem.getMotivoAnalise().map(MotivoAnalise::codigo).orElse(null);
        destino.motivoAnaliseMensagem = origem.getMotivoAnalise().map(MotivoAnalise::mensagem).orElse(null);
        destino.motivoCancelamento = origem.getMotivoCancelamento().map(Motivo::texto).orElse(null);
    }

    static Pagamento paraDominio(PagamentoJpaEntity e) {
        return Pagamento.reconstituir(
                new PagamentoId(e.id),
                e.modalidade,
                e.criadoEm,
                new BoletosDoPagamento(e.boletos.stream().map(PagamentoMapper::boleto).toList()),
                e.agenciaDebito == null ? null : new ContaDebito(e.agenciaDebito, e.contaDebito),
                e.dataPagamento,
                e.status,
                e.envios,
                e.idTransacao,
                e.autenticacao,
                e.motivoRecusa,
                e.motivoAnaliseCodigo == null ? null : new MotivoAnalise(e.motivoAnaliseCodigo, e.motivoAnaliseMensagem),
                e.motivoCancelamento == null ? null : new Motivo(e.motivoCancelamento));
    }

    private static BoletoJpa boletoJpa(BoletoAPagar b) {
        var j = new BoletoJpa();
        j.idNoEmissor = b.idNoEmissor();
        j.linhaDigitavel = b.linhaDigitavel().valor();
        j.valor = b.valor().valor();
        j.vencimento = b.vencimento();
        j.beneficiario = b.beneficiario();
        return j;
    }

    private static BoletoAPagar boleto(BoletoJpa j) {
        return new BoletoAPagar(j.idNoEmissor, new LinhaDigitavel(j.linhaDigitavel), new Dinheiro(j.valor),
                j.vencimento, j.beneficiario);
    }
}
