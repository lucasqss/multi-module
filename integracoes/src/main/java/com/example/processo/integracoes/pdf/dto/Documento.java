package com.example.processo.integracoes.pdf.dto;

import com.example.processo.core.motor.domain.model.Processo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pedido ao sistema de PDF, no formato dele: qual modelo usar, a referência do documento e os dados que preenchem o
 * modelo. Só tipos simples, nenhum tipo do domínio: renomear algo no core não muda o contrato com o sistema externo
 * sem passar por aqui. Os dados próprios de cada segmento seguem achatados em {@code campos}.
 */
public record Documento(String modelo, String referencia, String segmento, String proponente, String cpf,
                        BigDecimal valor, LocalDate dataAbertura, Map<String, Object> campos) {

    /** O modelo é escolhido pelo código do segmento; a referência é o número do processo. */
    public static Documento deAbertura(Processo<?> processo) {
        return new Documento("abertura-processo-" + processo.segmento().toLowerCase(), processo.numero(),
                processo.segmento(), processo.proponente().nome(), processo.proponente().cpf(), processo.valor(),
                processo.dataAbertura(), campos(processo.dados()));
    }

    /** Um campo por componente do record de dados do segmento, na ordem declarada; enum vai pelo nome. */
    private static Map<String, Object> campos(Object dados) {
        if (!(dados instanceof Record)) {
            throw new IllegalArgumentException("Dados do segmento devem ser um record: " + dados.getClass().getName());
        }
        var campos = new LinkedHashMap<String, Object>();
        for (RecordComponent componente : dados.getClass().getRecordComponents()) {
            Object valor = ler(componente, dados);
            campos.put(componente.getName(), valor instanceof Enum<?> e ? e.name() : valor);
        }
        return campos;
    }

    private static Object ler(RecordComponent componente, Object dados) {
        try {
            var acessor = componente.getAccessor();
            acessor.setAccessible(true); // records de segmento podem não ser públicos (ex.: nos testes)
            return acessor.invoke(dados);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Não foi possível ler " + componente.getName() + " de " + dados, e);
        }
    }
}
