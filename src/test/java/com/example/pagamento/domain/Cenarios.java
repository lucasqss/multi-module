package com.example.pagamento.domain;

import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.specification.pagamento.SubmissaoPagamento;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.LinhaDigitavel;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.domain.valueobject.RetornoBanco;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Test data builder: pagamento VÁLIDO (pronto para submeter) por padrão; cada teste muda só o que interessa. */
public final class Cenarios {

    /** Terça-feira. */
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    public static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");

    private final Modalidade modalidade;
    private final List<BoletoAPagar> boletos = new ArrayList<>();
    private ContaDebito conta = new ContaDebito("1234", "998877");
    private LocalDate dataPagamento = HOJE.plusDays(7); // terça

    private Cenarios(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    /** INDIVIDUAL com 1 boleto de R$ 500,00. */
    public static Cenarios individual() {
        return new Cenarios(Modalidade.INDIVIDUAL).comBoleto(1, "500.00", "Fornecedor A");
    }

    /** PROCESSO com 2 boletos do mesmo beneficiário. */
    public static Cenarios processo() {
        return new Cenarios(Modalidade.PROCESSO).comBoleto(1, "300.00", "Fornecedor A").comBoleto(2, "200.00", "Fornecedor A");
    }

    /** LOTE com 2 boletos de beneficiários diferentes. */
    public static Cenarios lote() {
        return new Cenarios(Modalidade.LOTE).comBoleto(1, "300.00", "Fornecedor A").comBoleto(2, "200.00", "Fornecedor B");
    }

    /** Um boleto como o emissor devolveria. */
    public static BoletoAPagar boleto(int id, String valor, LocalDate vencimento, String beneficiario) {
        var linha = "9001" + "%025d".formatted(id) + vencimento.toString().replace("-", "") + "0".repeat(10);
        return new BoletoAPagar("EMI-" + id, new LinhaDigitavel(linha), Dinheiro.de(valor), vencimento, beneficiario);
    }

    public Cenarios comBoleto(int id, String valor, String beneficiario) {
        boletos.add(boleto(id, valor, HOJE.plusDays(10), beneficiario));
        return this;
    }

    /** Troca os boletos por {@code quantidade} boletos de R$ 10,00 (beneficiário único). */
    public Cenarios comBoletos(int quantidade) {
        boletos.clear();
        for (int i = 1; i <= quantidade; i++) {
            comBoleto(i, "10.00", "Fornecedor A");
        }
        return this;
    }

    public Cenarios semBoletos() { boletos.clear(); return this; }
    public Cenarios vencimentoDoBoleto(LocalDate v) { boletos.set(0, boleto(1, "500.00", v, "Fornecedor A")); return this; }
    public Cenarios semConta() { conta = null; return this; }
    public Cenarios dataPagamento(LocalDate d) { dataPagamento = d; return this; }

    public BoletosDoPagamento boletos() {
        return new BoletosDoPagamento(boletos);
    }

    public SubmissaoPagamento submissao() {
        return new SubmissaoPagamento(modalidade, boletos(), conta, dataPagamento, HOJE);
    }

    /** Rascunho (EM_EDICAO) com as seções preenchidas. */
    public Pagamento rascunho() {
        var p = Pagamento.criar(modalidade, AGORA);
        p.salvarBoletos(boletos());
        p.alterarDadosGerais(conta, dataPagamento);
        return p;
    }

    public Pagamento agendado() {
        var p = rascunho();
        p.submeter(HOJE);
        return p;
    }

    public Pagamento enviado() {
        var p = agendado();
        p.enviarAoBanco();
        return p;
    }

    public static RetornoBanco liquidado(Pagamento p, String valor) {
        return new RetornoBanco(p.getIdTransacao().orElseThrow(), true, Dinheiro.de(valor), "AUT-1", null, AGORA);
    }

    public static RetornoBanco recusado(Pagamento p) {
        return new RetornoBanco(p.getIdTransacao().orElseThrow(), false, null, null, "saldo insuficiente", AGORA);
    }
}
