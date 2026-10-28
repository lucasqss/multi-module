package com.example.processo.integracao.pdf;

import com.example.processo.modelo.Processo;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pedido ao sistema de PDF, no formato dele: qual modelo usar, a referência do documento e os dados que preenchem o
 * modelo. Só tipos simples: renomear algo no modelo não muda o contrato com o sistema externo sem passar por aqui. Os
 * dados próprios de cada modalidade seguem achatados em {@code campos}.
 */
public record Documento(String modelo, String referencia, String segmento, String proponente, String cpf,
                        BigDecimal valor, LocalDate dataAbertura, Map<String, Object> campos) {

    /** O modelo é escolhido pelo segmento do processo; a referência é o número. */
    public static Documento deAbertura(Processo processo) {
        return new Documento("abertura-processo-" + processo.getSegmento().toLowerCase(), processo.getNumero(),
                processo.getSegmento(), processo.getProponenteNome(), processo.getProponenteCpf(), processo.getValor(),
                processo.getDataAbertura(), campos(processo));
    }

    /**
     * Um campo por atributo da modalidade (os declarados na subclasse de {@link Processo}), na ordem declarada; enum vai
     * pelo nome. Modalidade nova entra no PDF sem mudar esta classe.
     */
    private static Map<String, Object> campos(Processo processo) {
        var campos = new LinkedHashMap<String, Object>();
        for (Field campo : processo.getClass().getDeclaredFields()) {
            if (daModalidade(campo)) {
                Object valor = ler(campo, processo);
                campos.put(campo.getName(), valor instanceof Enum<?> e ? e.name() : valor);
            }
        }
        return campos;
    }

    /**
     * Fora ficam constantes ({@code SEGMENTO}) e o que o enhancement do Hibernate acrescenta à classe em tempo de build
     * ({@code $$_hibernate_...}, sintéticos ou transientes).
     */
    private static boolean daModalidade(Field campo) {
        int modificadores = campo.getModifiers();
        return !Modifier.isStatic(modificadores) && !Modifier.isTransient(modificadores) && !campo.isSynthetic()
                && !campo.getName().startsWith("$$");
    }

    private static Object ler(Field campo, Processo processo) {
        try {
            campo.setAccessible(true);
            return campo.get(processo);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Não foi possível ler " + campo.getName() + " de " + processo.getNumero(), e);
        }
    }
}
