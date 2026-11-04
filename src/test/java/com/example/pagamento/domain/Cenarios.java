package com.example.pagamento.domain;

import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.specification.processo.SubmissaoProcesso;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.LinhaDigitavel;
import com.example.pagamento.domain.valueobject.Matricula;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Test data builder: processo VÁLIDO (pronto para submeter) por padrão; cada teste muda só o que interessa. */
public final class Cenarios {

    /** Terça-feira. */
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    public static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");
    public static final Matricula SOLICITANTE = new Matricula("F1234567");
    public static final Matricula APROVADOR_1 = new Matricula("A1111111");
    public static final Matricula APROVADOR_2 = new Matricula("A2222222");

    private final List<BoletoAPagar> boletos = new ArrayList<>(List.of(boleto(1, "500.00", HOJE.plusDays(10))));
    private ContaDebito conta = new ContaDebito("1234", "998877");
    private LocalDate dataPagamento = HOJE.plusDays(7); // terça

    public static Cenarios processo() {
        return new Cenarios();
    }

    /** Um boleto como o emissor devolveria. */
    public static BoletoAPagar boleto(int id, String valor, LocalDate vencimento) {
        var linha = "9001" + "%025d".formatted(id) + vencimento.toString().replace("-", "") + "0".repeat(10);
        return new BoletoAPagar("EMI-" + id, new LinhaDigitavel(linha), Dinheiro.de(valor), vencimento, "Fornecedor " + id);
    }

    public Cenarios semBoletos() { boletos.clear(); return this; }
    public Cenarios comBoleto(int id, String valor) { boletos.add(boleto(id, valor, HOJE.plusDays(10))); return this; }
    public Cenarios valor(String v) { boletos.clear(); boletos.add(boleto(1, v, HOJE.plusDays(10))); return this; }
    public Cenarios vencimentoDoBoleto(LocalDate v) { boletos.clear(); boletos.add(boleto(1, "500.00", v)); return this; }
    public Cenarios semConta() { conta = null; return this; }
    public Cenarios dataPagamento(LocalDate d) { dataPagamento = d; return this; }

    public BoletosDoProcesso boletos() {
        return new BoletosDoProcesso(boletos);
    }

    public SubmissaoProcesso submissao() {
        return new SubmissaoProcesso(boletos(), conta, dataPagamento, HOJE);
    }

    /** Rascunho (EM_EDICAO) com as seções preenchidas. */
    public ProcessoPagamento rascunho() {
        var p = ProcessoPagamento.criar(SOLICITANTE, AGORA);
        if (!boletos.isEmpty()) {
            p.salvarBoletos(boletos());
        }
        p.alterarDadosGerais(conta, dataPagamento);
        return p;
    }

    public ProcessoPagamento submetido() {
        var p = rascunho();
        p.submeter(HOJE);
        return p;
    }

    /** Aprovado (valor padrão exige só uma aprovação). */
    public ProcessoPagamento aprovado() {
        var p = submetido();
        p.aprovar(APROVADOR_1, AGORA);
        return p;
    }

    public ProcessoPagamento enviado() {
        var p = aprovado();
        p.enviarParaPagamento(AGORA);
        return p;
    }
}
