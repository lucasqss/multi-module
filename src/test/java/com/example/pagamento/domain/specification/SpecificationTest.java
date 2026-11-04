package com.example.pagamento.domain.specification;

import com.example.pagamento.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static com.example.pagamento.domain.specification.Specification.quando;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    void validadorParaNaPrimeiraRegraVioladaELancaAExcecaoDela() {
        var validador = ValidadorRegras.<Integer>de(
                RegraNegocio.de(positivo, n -> new NaoPositivoException()),
                RegraNegocio.de(par, n -> new ImparException()));

        assertDoesNotThrow(() -> validador.validar(2));
        assertThrows(ImparException.class, () -> validador.validar(3));
        assertThrows(NaoPositivoException.class, () -> validador.validar(-3)); // viola as duas: vale a 1ª
    }

    @Test
    void primeiraViolacaoClassificaSemLancar() {
        var validador = ValidadorRegras.<Integer>de(
                RegraNegocio.de(positivo, n -> new NaoPositivoException()),
                RegraNegocio.de(par, n -> new ImparException()));

        assertTrue(validador.primeiraViolacao(2).isEmpty());
        assertInstanceOf(ImparException.class, validador.primeiraViolacao(3).orElseThrow());
        assertInstanceOf(NaoPositivoException.class, validador.primeiraViolacao(-3).orElseThrow());
    }

    @Test
    void cadeiaAplicaSoOsElosDoCandidatoNaOrdemDeclarada() {
        var cadeia = CadeiaValidacao.comecandoPor(ValidadorRegras.<Integer>de(
                        RegraNegocio.de(positivo, n -> new NaoPositivoException())))
                .depois(n -> n > 100, ValidadorRegras.de(RegraNegocio.de(par, n -> new ImparException())));

        assertDoesNotThrow(() -> cadeia.validar(3));     // elo "> 100" não se aplica a 3
        assertDoesNotThrow(() -> cadeia.validar(102));
        assertThrows(ImparException.class, () -> cadeia.validar(101));
        assertThrows(NaoPositivoException.class, () -> cadeia.validar(-1)); // 1º elo barra antes
    }

    static class NaoPositivoException extends RegraNegocioException {
        NaoPositivoException() {
            super("NAO_POSITIVO", "não é positivo");
        }
    }

    static class ImparException extends RegraNegocioException {
        ImparException() {
            super("IMPAR", "é ímpar");
        }
    }
}
