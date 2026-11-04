package com.example.financiamento.adapter.avaliacao;

import com.example.financiamento.domain.exception.BemNaoEncontradoNaAvaliacaoException;
import com.example.financiamento.domain.valueobject.Bem;
import com.example.financiamento.domain.valueobject.Dinheiro;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

/**
 * Integração de SAÍDA com o SISTEMA DE AVALIAÇÃO de bens (externo): devolve o bem já no formato do
 * domínio ({@link Bem}); erros do sistema viram exceções do domínio. O UC injeta direto, sem interface.
 *
 * <p>SIMULAÇÃO (na vida real: laudo de avaliação para imóvel, tabela de referência para veículo):
 * <ul>
 *   <li>IMÓVEL: valor avaliado = número da matrícula × R$ 1.000 (matrícula 500 → R$ 500.000);</li>
 *   <li>VEÍCULO: valor fixo de R$ 100.000; ano-modelo = 2000 + os 2 últimos dígitos da placa;</li>
 *   <li>matrícula começando com 0 ou placa começando com ZZZ: o sistema não conhece o bem.</li>
 * </ul>
 */
@ApplicationScoped
public class AvaliadorDeBens {

    public Bem avaliar(TipoFinanciamento tipo, String identificacao) {
        var id = tipo.identificacaoValida(identificacao);
        if (id.startsWith("0") || id.startsWith("ZZZ")) {
            throw new BemNaoEncontradoNaAvaliacaoException(id);
        }
        return switch (tipo) { // tradução do contrato externo, por tipo: é papel do adapter
            case IMOVEL -> new Bem(id, "Imóvel da matrícula " + id,
                    new Dinheiro(new BigDecimal(id).multiply(BigDecimal.valueOf(1_000))), null);
            case VEICULO -> {
                var ano = 2000 + Integer.parseInt(id.substring(id.length() - 2));
                yield new Bem(id, "Veículo placa %s, ano %d".formatted(id, ano), Dinheiro.de("100000.00"), ano);
            }
        };
    }
}
