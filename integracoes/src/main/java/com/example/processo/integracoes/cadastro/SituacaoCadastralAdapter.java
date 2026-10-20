package com.example.processo.integracoes.cadastro;

import com.example.processo.core.motor.domain.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.SituacaoCadastral;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
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

    /** Erro HTTP (WebApplicationException) ou de rede/timeout (ProcessingException) vira erro do domínio. */
    @Override
    public boolean regular(String cpf) {
        try {
            return RespostaCadastro.REGULAR.equals(cliente.consultar(cpf).situacao());
        } catch (WebApplicationException | ProcessingException e) {
            throw new SistemaExternoIndisponivelException("de cadastro", e);
        }
    }
}
