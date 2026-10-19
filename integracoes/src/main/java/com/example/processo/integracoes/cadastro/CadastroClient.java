package com.example.processo.integracoes.cadastro;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Cliente do sistema externo de cadastro. Simulado: responde sempre REGULAR. Para a chamada real, troque esta
 * classe por uma interface {@code @RegisterRestClient(configKey = "cadastro")} com o mesmo método e configure
 * {@code quarkus.rest-client.cadastro.url}; o adapter não muda.
 */
@ApplicationScoped
public class CadastroClient {

    public RespostaCadastro consultar(String documento) {
        return new RespostaCadastro(documento, RespostaCadastro.REGULAR);
    }
}
