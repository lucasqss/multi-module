package com.example.boleto.domain;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.specification.boleto.CondicoesEmissao;
import com.example.boleto.domain.specification.boleto.EmissaoBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.ItemBoleto;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.valueobject.Pagador;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Test data builder: cenário VÁLIDO (pronto para emitir) por padrão; cada teste muda só o que interessa. */
public final class Cenarios {

    /** Terça-feira. */
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    public static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");
    public static final String CPF = "12345678901";
    public static final String CNPJ = "12345678000199";
    public static final String EMAIL_PAGADOR = "maria@example.com";

    private String documento = CPF;
    private final List<ItemBoleto> itens = new ArrayList<>(List.of(ItemBoleto.de("Mensalidade", new BigDecimal("500.00"))));
    private LocalDate vencimento = HOJE.plusDays(7); // terça
    private long boletosEmAberto;

    public static Cenarios boleto() {
        return new Cenarios();
    }

    public Cenarios documento(String v) { documento = v; return this; }
    public Cenarios vencimento(LocalDate v) { vencimento = v; return this; }
    public Cenarios boletosEmAberto(long v) { boletosEmAberto = v; return this; }

    /** Substitui os itens por um único item com o valor informado. */
    public Cenarios valor(String v) {
        itens.clear();
        itens.add(new ItemBoleto("Item", Dinheiro.de(v)));
        return this;
    }

    public Cenarios semItens() { itens.clear(); return this; }

    public Pagador pagador() {
        return Pagador.de("Maria Silva", documento, EMAIL_PAGADOR);
    }

    public CondicoesEmissao condicoes() {
        return new CondicoesEmissao(AGORA, HOJE, 60, boletosEmAberto);
    }

    /** Candidato das regras de emissão. */
    public EmissaoBoleto emissao() {
        return new EmissaoBoleto(pagador(), new ItensBoleto(itens), vencimento, condicoes());
    }

    /** Rascunho (EM_EDICAO) com as seções preenchidas, ainda não emitido. */
    public Boleto rascunho() {
        var boleto = Boleto.criar(pagador(), vencimento, AGORA);
        if (!itens.isEmpty()) {
            boleto.salvarItens(new ItensBoleto(itens));
        }
        return boleto;
    }

    public Boleto emitido() {
        var boleto = rascunho();
        boleto.emitir(condicoes());
        return boleto;
    }
}
