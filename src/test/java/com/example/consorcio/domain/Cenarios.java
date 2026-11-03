package com.example.consorcio.domain;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.DadosPagamento;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;

import java.time.Instant;
import java.time.LocalDate;

/** Test data builder: cenário VÁLIDO por padrão; cada teste muda só o que interessa. */
public final class Cenarios {

    /** Terça-feira. */
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    public static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");
    public static final IdentificadorCota ID_COTA = new IdentificadorCota(1234, 10);

    private SituacaoCota situacao = SituacaoCota.ATIVA;
    private String saldoDevedor = "15000.00";
    private String valor = "500.00";
    private FormaPagamento forma = FormaPagamento.BOLETO;
    private LocalDate vencimento = HOJE.plusDays(7); // terça
    private ContaDebito conta;
    private boolean possuiEmAndamento;

    public static Cenarios solicitacao() {
        return new Cenarios();
    }

    public Cenarios situacao(SituacaoCota v) { situacao = v; return this; }
    public Cenarios saldoDevedor(String v) { saldoDevedor = v; return this; }
    public Cenarios valor(String v) { valor = v; return this; }
    public Cenarios forma(FormaPagamento v) { forma = v; return this; }
    public Cenarios vencimento(LocalDate v) { vencimento = v; return this; }
    public Cenarios conta(String agencia, String numero) { conta = new ContaDebito(agencia, numero); return this; }
    public Cenarios possuiEmAndamento(boolean v) { possuiEmAndamento = v; return this; }

    public SolicitacaoPagamento build() {
        var cota = new Cota(ID_COTA, situacao, Dinheiro.de(saldoDevedor));
        var dados = new DadosPagamento(Dinheiro.de(valor), forma, vencimento, conta);
        return new SolicitacaoPagamento(cota, dados, AGORA, HOJE, 30, possuiEmAndamento);
    }

    public ProcessoPagamento abrir() {
        return ProcessoPagamento.abrir(build());
    }
}
