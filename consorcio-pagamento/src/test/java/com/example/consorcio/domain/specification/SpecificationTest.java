package com.example.consorcio.domain.specification;

import org.junit.jupiter.api.Test;

import static com.example.consorcio.domain.specification.Specification.quando;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa a infraestrutura genérica do pattern, com um tipo simples (Integer). */
class SpecificationTest {

    private final Specification<Integer> positivo = n -> n > 0;
    private final Specification<Integer> par = n -> n % 2 == 0;

    @Test
    void composicaoAndOrNot() {
        assertTrue(positivo.and(par).isSatisfiedBy(4));
        assertFalse(positivo.and(par).isSatisfiedBy(3));
        assertTrue(positivo.or(par).isSatisfiedBy(-2));
        assertTrue(positivo.not().isSatisfiedBy(-1));
    }

    @Test
    void quandoEntaoEhImplicacao() {
        var seParEntaoPositivo = quando(par).entao(positivo);
        assertTrue(seParEntaoPositivo.isSatisfiedBy(4));   // par e positivo
        assertTrue(seParEntaoPositivo.isSatisfiedBy(-3));  // não é par: regra não se aplica
        assertFalse(seParEntaoPositivo.isSatisfiedBy(-4)); // par e negativo
    }

    @Test
    void validadorAcumulaTodasAsViolacoes() {
        var validador = ValidadorRegras.<Integer>de(
                RegraNegocio.de("NAO_POSITIVO", positivo, n -> n + " não é positivo"),
                RegraNegocio.de("IMPAR", par, n -> n + " é ímpar"));

        assertEquals(0, validador.avaliar(2).size());
        var erro = assertThrows(com.example.consorcio.domain.exception.RegraNegocioException.class,
                () -> validador.validar(-3));
        assertEquals(2, erro.getViolacoes().size());
        assertEquals("REGRAS_DE_NEGOCIO_VIOLADAS", erro.getCodigo());
    }
}
