package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.exception.AprovadorRepetidoException;
import com.example.pagamento.domain.exception.BoletoCanceladoNoEmissorException;
import com.example.pagamento.domain.exception.ProcessoSemBoletosException;
import com.example.pagamento.domain.exception.QuantidadeMaximaBoletosExcedidaException;
import com.example.pagamento.domain.exception.TransicaoEstadoInvalidaException;
import com.example.pagamento.domain.exception.ValorLiquidadoDivergenteException;
import com.example.pagamento.domain.state.StatusProcesso;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.RetornoBanco;
import com.example.pagamento.domain.valueobject.StatusTentativa;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.example.pagamento.domain.Cenarios.AGORA;
import static com.example.pagamento.domain.Cenarios.APROVADOR_1;
import static com.example.pagamento.domain.Cenarios.APROVADOR_2;
import static com.example.pagamento.domain.Cenarios.HOJE;
import static com.example.pagamento.domain.Cenarios.boleto;
import static com.example.pagamento.domain.Cenarios.processo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Garante que o agregado APLICA estado e regras e cuida das suas partes/entidades internas. */
class ProcessoPagamentoTest {

    private static RetornoBanco liquidado(ProcessoPagamento p, String valor) {
        return new RetornoBanco(p.getTentativaAtual().orElseThrow().getIdTransacao(), true, Dinheiro.de(valor),
                "AUT-1", null, AGORA);
    }

    private static RetornoBanco recusado(ProcessoPagamento p) {
        return new RetornoBanco(p.getTentativaAtual().orElseThrow().getIdTransacao(), false, null, null,
                "saldo insuficiente", AGORA);
    }

    // ===================== edição por partes =====================

    @Test
    void nasceEmEdicaoIncompletoEOValorEhASomaDosBoletos() {
        var p = processo().comBoleto(2, "250.00").rascunho();
        assertEquals(StatusProcesso.EM_EDICAO, p.getStatus());
        assertEquals(Dinheiro.de("750.00"), p.getValorTotal());
    }

    @Test
    void secaoInvalidaNaoMudaNada() {
        var p = processo().rascunho();
        var muitos = new BoletosDoProcesso(IntStream.rangeClosed(1, 21).mapToObj(i -> boleto(i, "1.00", HOJE)).toList());
        assertThrows(QuantidadeMaximaBoletosExcedidaException.class, () -> p.salvarBoletos(muitos));
        assertEquals(1, p.getBoletos().quantidade());
    }

    @Test
    void submissaoExigeCompletude() {
        var p = processo().semBoletos().rascunho();
        assertThrows(ProcessoSemBoletosException.class, () -> p.submeter(HOJE));
        assertEquals(StatusProcesso.EM_EDICAO, p.getStatus());
    }

    @Test
    void depoisDeSubmetidoNaoEdita_estadoAntesDasRegras() {
        var p = processo().submetido();
        assertThrows(TransicaoEstadoInvalidaException.class, () -> p.salvarBoletos(BoletosDoProcesso.NENHUM));
    }

    // ===================== aprovação (entidade interna) e alçada =====================

    @Test
    void valorAteDezMilPrecisaDeUmaAprovacao() {
        var p = processo().submetido();
        p.aprovar(APROVADOR_1, AGORA);
        assertEquals(StatusProcesso.APROVADO, p.getStatus());
        assertEquals(1, p.getAprovacoes().size());
    }

    @Test
    void valorAcimaDeDezMilPrecisaDeDuas() {
        var p = processo().valor("10000.01").submetido();
        p.aprovar(APROVADOR_1, AGORA);
        assertEquals(StatusProcesso.AGUARDANDO_APROVACAO, p.getStatus()); // auto-transição: falta uma
        assertThrows(AprovadorRepetidoException.class, () -> p.aprovar(APROVADOR_1, AGORA));
        p.aprovar(APROVADOR_2, AGORA);
        assertEquals(StatusProcesso.APROVADO, p.getStatus());
    }

    @Test
    void rejeitarEReabrirDescartaAsAprovacoes() {
        var p = processo().valor("20000.00").submetido();
        p.aprovar(APROVADOR_1, AGORA);
        p.rejeitar(new Motivo("conta errada"));
        p.reabrir();
        assertEquals(StatusProcesso.EM_EDICAO, p.getStatus());
        assertTrue(p.getAprovacoes().isEmpty());
    }

    // ===================== banco (tentativas = entidades internas; retorno = fato) =====================

    @Test
    void envioCriaTentativaERetornoConformeQuita() {
        var p = processo().enviado();
        var tentativa = p.getTentativaAtual().orElseThrow();
        assertEquals(StatusTentativa.ENVIADA, tentativa.getStatus());

        p.registrarRetornoBanco(liquidado(p, "500.00"));

        assertEquals(StatusProcesso.PAGO, p.getStatus());
        assertEquals(StatusTentativa.LIQUIDADA, p.getTentativaAtual().orElseThrow().getStatus());
    }

    @Test
    void recusaPermiteRetentarComNovaTentativa() {
        var p = processo().enviado();
        p.registrarRetornoBanco(recusado(p));
        assertEquals(StatusProcesso.FALHOU, p.getStatus());

        p.retentarPagamento(AGORA);
        p.registrarRetornoBanco(liquidado(p, "500.00"));

        assertEquals(StatusProcesso.PAGO, p.getStatus());
        assertEquals(2, p.getTentativas().size());
        assertEquals(StatusTentativa.RECUSADA, p.getTentativas().getFirst().getStatus());
    }

    @Test
    void liquidadoDivergenteNaoEhRecusado_vaiParaAnalise() {
        var p = processo().enviado();
        p.registrarRetornoBanco(liquidado(p, "499.00")); // não lança: o dinheiro já saiu
        assertEquals(StatusProcesso.EM_ANALISE, p.getStatus());
        assertEquals(ValorLiquidadoDivergenteException.CODIGO, p.getMotivoAnalise().orElseThrow().codigo());
    }

    @Test
    void retornoRepetidoEhIgnorado() {
        var p = processo().enviado();
        var retorno = liquidado(p, "500.00");
        p.registrarRetornoBanco(retorno);
        p.registrarRetornoBanco(retorno); // webhook reenviado
        assertEquals(StatusProcesso.PAGO, p.getStatus());
    }

    @Test
    void usuarioNaoCancelaEnquantoEstaNoBanco() {
        var p = processo().enviado();
        assertThrows(TransicaoEstadoInvalidaException.class, () -> p.cancelar(new Motivo("desisti")));
    }

    // ===================== fatos do emissor =====================

    @Test
    void boletoCanceladoNoEmissorAntesDoEnvioCancelaOProcesso() {
        var p = processo().aprovado();
        p.registrarBoletoCanceladoNoEmissor("EMI-1");
        assertEquals(StatusProcesso.CANCELADO, p.getStatus());
        assertTrue(p.getMotivoCancelamento().orElseThrow().texto().contains("EMI-1"));
    }

    @Test
    void boletoCanceladoNoEmissorDepoisDoEnvioVaiParaAnalise() {
        var p = processo().enviado();
        p.registrarBoletoCanceladoNoEmissor("EMI-1");
        assertEquals(StatusProcesso.EM_ANALISE, p.getStatus());
        assertEquals(BoletoCanceladoNoEmissorException.CODIGO, p.getMotivoAnalise().orElseThrow().codigo());

        p.registrarRetornoBanco(liquidado(p, "500.00")); // o fato é registrado, mas continua em análise
        assertEquals(StatusProcesso.EM_ANALISE, p.getStatus());
        assertEquals(StatusTentativa.LIQUIDADA, p.getTentativaAtual().orElseThrow().getStatus());
    }
}
