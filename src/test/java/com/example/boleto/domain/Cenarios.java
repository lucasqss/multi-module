package com.example.boleto.domain;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.model.SolicitacaoBoleto;
import com.example.boleto.domain.valueobject.DadosBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Pagador;

import java.time.Instant;
import java.time.LocalDate;

/** Test data builder: cenário VÁLIDO por padrão; cada teste muda só o que interessa. */
public final class Cenarios {

    /** Terça-feira. */
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    public static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");
    public static final String CPF = "12345678901";
    public static final String CNPJ = "12345678000199";

    private String documento = CPF;
    private String valor = "500.00";
    private LocalDate vencimento = HOJE.plusDays(7); // terça
    private long boletosEmAberto;

    public static Cenarios solicitacao() {
        return new Cenarios();
    }

    public Cenarios documento(String v) { documento = v; return this; }
    public Cenarios valor(String v) { valor = v; return this; }
    public Cenarios vencimento(LocalDate v) { vencimento = v; return this; }
    public Cenarios boletosEmAberto(long v) { boletosEmAberto = v; return this; }

    public SolicitacaoBoleto build() {
        var pagador = Pagador.de("Maria Silva", documento, "maria@example.com");
        var dados = new DadosBoleto(pagador, Dinheiro.de(valor), vencimento);
        return new SolicitacaoBoleto(dados, AGORA, HOJE, 60, boletosEmAberto);
    }

    public Boleto emitir() {
        return Boleto.emitir(build());
    }
}
