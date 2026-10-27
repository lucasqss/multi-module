package com.example.processo.core.motor.domain.rule;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** O que sobrou para as regras comuns: CPF e valor já chegam válidos (value objects). */
class RegrasComunsTest {

    record Dados() implements DadosDoSegmento {
    }

    static Optional<String> violacao(Proponente proponente, Valor valor) {
        return RegrasComuns.<Dados>todas().violacao(new Solicitacao<>(proponente, valor, new Dados()));
    }

    @Test
    void nomeObrigatorio() {
        var valor = new Valor("1000");
        assertEquals(Optional.of("Nome do proponente é obrigatório"), violacao(null, valor));
        assertEquals(Optional.of("Nome do proponente é obrigatório"), violacao(new Proponente(" ", "52998224725"), valor));
        assertEquals(Optional.empty(), violacao(new Proponente("Ana Souza", "52998224725"), valor));
    }

    @Test
    void valorObrigatorio() {
        assertEquals(Optional.of("Valor do processo deve ser maior que zero"),
                violacao(new Proponente("Ana Souza", "52998224725"), null));
    }
}
