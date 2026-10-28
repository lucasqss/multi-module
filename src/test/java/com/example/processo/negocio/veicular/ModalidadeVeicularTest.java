package com.example.processo.negocio.veicular;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.Valor;
import com.example.processo.negocio.processo.Avaliacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** As regras e a decisão da modalidade veicular, em Java puro, com a data fixa. */
class ModalidadeVeicularTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 19);
    static final Clock RELOGIO = Clock.fixed(Instant.from(HOJE.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC);

    final ModalidadeVeicular modalidade = new ModalidadeVeicular(RELOGIO);

    static ProcessoVeicular pedido(String valor, String placa, Integer ano, String fipe) {
        return new ProcessoVeicular("Ana Souza", new Cpf("52998224725"), new Valor(valor), placa, ano,
                fipe == null ? null : new BigDecimal(fipe));
    }

    static Optional<String> violacao(ProcessoVeicular pedido) {
        return RegrasVeiculares.TODAS.violacao(pedido);
    }

    @Test
    void placaAntigaEMercosulSaoAceitas() {
        assertEquals(Optional.empty(), violacao(pedido("1000", "ABC1234", 2022, "50000")));
        assertEquals(Optional.empty(), violacao(pedido("1000", "ABC1D23", 2022, "50000")));
    }

    @Test
    void dadosDoVeiculoInvalidos() {
        assertEquals(Optional.of("Placa inválida"), violacao(pedido("1000", "abc-1234", 2022, "50000")));
        assertEquals(Optional.of("Ano de fabricação inválido"), violacao(pedido("1000", "ABC1234", null, "50000")));
        assertEquals(Optional.of("Valor FIPE deve ser maior que zero"), violacao(pedido("1000", "ABC1234", 2022, "0")));
        assertEquals(Optional.of("Valor excede 90% da FIPE (máximo 45000.00)"),
                violacao(pedido("45000.01", "ABC1234", 2022, "50000")));
    }

    @Test
    void processamentoRecusaVeiculoComMaisDeDezAnos() {
        assertEquals(Avaliacao.aprovado(), modalidade.avaliar(pedido("1000", "ABC1234", 2016, "50000")));
        assertEquals(Avaliacao.recusado("Veículo com mais de 10 anos (fabricado em 2015)"),
                modalidade.avaliar(pedido("1000", "ABC1234", 2015, "50000")));
    }
}
