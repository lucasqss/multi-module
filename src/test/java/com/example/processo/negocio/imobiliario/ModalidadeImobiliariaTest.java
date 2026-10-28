package com.example.processo.negocio.imobiliario;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.Valor;
import com.example.processo.negocio.processo.Avaliacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * As regras e a decisão da modalidade, em Java puro: sem Quarkus nem banco. A regra que consulta o banco (matrícula em
 * andamento) é coberta no {@code CriarProcessoImobiliarioIT}.
 */
class ModalidadeImobiliariaTest {

    final ModalidadeImobiliaria modalidade = new ModalidadeImobiliaria(null);

    static ProcessoImobiliario pedido(String valor, String matricula, TipoImovel tipo, String avaliacao, String uf) {
        return new ProcessoImobiliario("Ana Souza", new Cpf("52998224725"), new Valor(valor), matricula, tipo,
                avaliacao == null ? null : new BigDecimal(avaliacao), uf);
    }

    static ProcessoImobiliario pedido(String valor, TipoImovel tipo, String avaliacao) {
        return pedido(valor, "12345", tipo, avaliacao, "SP");
    }

    static Optional<String> violacao(ProcessoImobiliario pedido) {
        return RegrasImobiliarias.TODAS.violacao(pedido);
    }

    @Test
    void limiteDaAvaliacaoDependeDoTipoDoImovel() {
        assertEquals(Optional.empty(), violacao(pedido("400000.00", TipoImovel.RESIDENCIAL, "500000.00")));
        assertEquals(Optional.empty(), violacao(pedido("300000.00", TipoImovel.COMERCIAL, "500000.00")));
        assertEquals(Optional.of("Valor excede 80% da avaliação para imóvel residencial (máximo 400000.00)"),
                violacao(pedido("400000.01", TipoImovel.RESIDENCIAL, "500000.00")));
        assertEquals(Optional.of("Valor excede 60% da avaliação para imóvel comercial (máximo 300000.00)"),
                violacao(pedido("300000.01", TipoImovel.COMERCIAL, "500000.00")));
    }

    @Test
    void dadosDoImovelInvalidos() {
        assertEquals(Optional.of("Matrícula do imóvel deve ter de 1 a 20 dígitos"),
                violacao(pedido("1000", "12-A", TipoImovel.RESIDENCIAL, "500000", "SP")));
        assertEquals(Optional.of("UF do imóvel inválida"),
                violacao(pedido("1000", "12", TipoImovel.RESIDENCIAL, "500000", "XX")));
        assertEquals(Optional.of("Tipo do imóvel é obrigatório"),
                violacao(pedido("1000", "12", null, "500000", "SP")));
        assertEquals(Optional.of("Valor de avaliação do imóvel deve ser maior que zero"),
                violacao(pedido("1000", "12", TipoImovel.COMERCIAL, "0", "SP")));
    }

    @Test
    void processamentoRecusaValorAcimaDoLimiteAutomatico() {
        assertEquals(Avaliacao.aprovado(),
                modalidade.avaliar(pedido("1000000.00", TipoImovel.RESIDENCIAL, "2000000.00")));
        assertEquals(Avaliacao.recusado("Valor acima de 1000000.00 exige análise manual"),
                modalidade.avaliar(pedido("1000000.01", TipoImovel.RESIDENCIAL, "2000000.00")));
    }
}
