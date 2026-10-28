package com.example.processo;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

/** Troca os canais da fila por memória: o teste do consumidor roda sem broker (e o Dev Services do AMQP não sobe). */
public class FilaEmMemoria implements QuarkusTestResourceLifecycleManager {

    static final String NOVOS = "processos-imobiliarios-novos";
    static final String RESPOSTAS = "processos-imobiliarios-respostas";

    @Override
    public Map<String, String> start() {
        var config = new HashMap<String, String>();
        config.putAll(InMemoryConnector.switchIncomingChannelsToInMemory(NOVOS));
        config.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory(RESPOSTAS));
        return config;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
