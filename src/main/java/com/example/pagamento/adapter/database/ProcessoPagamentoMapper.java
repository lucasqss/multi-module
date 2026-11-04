package com.example.pagamento.adapter.database;

import com.example.pagamento.adapter.database.ProcessoPagamentoJpaEntity.AprovacaoJpa;
import com.example.pagamento.adapter.database.ProcessoPagamentoJpaEntity.BoletoJpa;
import com.example.pagamento.adapter.database.ProcessoPagamentoJpaEntity.TentativaJpa;
import com.example.pagamento.domain.entity.Aprovacao;
import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.entity.TentativaPagamento;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.LinhaDigitavel;
import com.example.pagamento.domain.valueobject.Matricula;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;

/** Converte domínio <-> JPA. Único lugar que conhece os dois lados. */
final class ProcessoPagamentoMapper {

    private ProcessoPagamentoMapper() {
    }

    /** Copia o domínio para dentro de uma entidade JPA (nova ou já gerenciada). */
    static void copiar(ProcessoPagamento origem, ProcessoPagamentoJpaEntity destino) {
        destino.id = origem.getId().valor();
        destino.solicitante = origem.getSolicitante().valor();
        destino.criadoEm = origem.getCriadoEm();
        destino.agenciaDebito = origem.getContaDebito().map(ContaDebito::agencia).orElse(null);
        destino.contaDebito = origem.getContaDebito().map(ContaDebito::conta).orElse(null);
        destino.dataPagamento = origem.getDataPagamento().orElse(null);
        // coleções gerenciadas: limpar e repovoar (não trocar a instância da lista)
        destino.boletos.clear();
        origem.getBoletos().boletos().forEach(b -> destino.boletos.add(boletoJpa(b)));
        destino.aprovacoes.clear();
        origem.getAprovacoes().forEach(a -> destino.aprovacoes.add(aprovacaoJpa(a)));
        destino.tentativas.clear();
        origem.getTentativas().forEach(t -> destino.tentativas.add(tentativaJpa(t)));
        destino.status = origem.getStatus();
        destino.motivoRejeicao = origem.getMotivoRejeicao().map(Motivo::texto).orElse(null);
        destino.motivoCancelamento = origem.getMotivoCancelamento().map(Motivo::texto).orElse(null);
        destino.motivoAnaliseCodigo = origem.getMotivoAnalise().map(MotivoAnalise::codigo).orElse(null);
        destino.motivoAnaliseMensagem = origem.getMotivoAnalise().map(MotivoAnalise::mensagem).orElse(null);
    }

    static ProcessoPagamento paraDominio(ProcessoPagamentoJpaEntity e) {
        return ProcessoPagamento.reconstituir(
                new ProcessoPagamentoId(e.id),
                new Matricula(e.solicitante),
                e.criadoEm,
                e.agenciaDebito == null ? null : new ContaDebito(e.agenciaDebito, e.contaDebito),
                e.dataPagamento,
                new BoletosDoProcesso(e.boletos.stream().map(ProcessoPagamentoMapper::boleto).toList()),
                e.aprovacoes.stream().map(a -> Aprovacao.reconstituir(new Matricula(a.aprovador), a.momento)).toList(),
                e.tentativas.stream().map(ProcessoPagamentoMapper::tentativa).toList(),
                e.status,
                e.motivoRejeicao == null ? null : new Motivo(e.motivoRejeicao),
                e.motivoCancelamento == null ? null : new Motivo(e.motivoCancelamento),
                e.motivoAnaliseCodigo == null ? null : new MotivoAnalise(e.motivoAnaliseCodigo, e.motivoAnaliseMensagem));
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

    private static AprovacaoJpa aprovacaoJpa(Aprovacao a) {
        var j = new AprovacaoJpa();
        j.aprovador = a.getAprovador().valor();
        j.momento = a.getMomento();
        return j;
    }

    private static TentativaJpa tentativaJpa(TentativaPagamento t) {
        var j = new TentativaJpa();
        j.numero = t.getNumero();
        j.idTransacao = t.getIdTransacao();
        j.status = t.getStatus();
        j.enviadaEm = t.getEnviadaEm();
        j.valorLiquidado = t.getValorLiquidado().map(Dinheiro::valor).orElse(null);
        j.codigoAutenticacao = t.getCodigoAutenticacao().orElse(null);
        j.motivoRecusa = t.getMotivoRecusa().orElse(null);
        j.concluidaEm = t.getConcluidaEm().orElse(null);
        return j;
    }

    private static TentativaPagamento tentativa(TentativaJpa j) {
        return TentativaPagamento.reconstituir(j.numero, j.idTransacao, j.enviadaEm, j.status,
                j.valorLiquidado == null ? null : new Dinheiro(j.valorLiquidado),
                j.codigoAutenticacao, j.motivoRecusa, j.concluidaEm);
    }
}
