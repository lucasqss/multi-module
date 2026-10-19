package com.example.processo.integracoes.cadastro;

import com.example.processo.core.motor.domain.SituacaoCadastral;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Implementa o port do motor sobre o sistema de cadastro: chama o cliente e traduz a resposta externa para o
 * que o domínio entende. Sem este bean no deploy, o Quarkus falha no build (port sem implementação).
 */
@ApplicationScoped
public class SituacaoCadastralAdapter implements SituacaoCadastral {

    private final CadastroClient cliente;

    public SituacaoCadastralAdapter(@RestClient CadastroClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public boolean regular(String cpf) {
        return RespostaCadastro.REGULAR.equals(cliente.consultar(cpf).situacao());
    }
}
