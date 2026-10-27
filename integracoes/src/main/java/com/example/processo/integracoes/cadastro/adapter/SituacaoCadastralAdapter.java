package com.example.processo.integracoes.cadastro.adapter;

import com.example.processo.core.motor.application.port.out.SituacaoCadastral;
import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.model.Cpf;
import com.example.processo.integracoes.cadastro.client.CadastroClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Implementa o port do motor sobre o sistema de cadastro: chama o cliente e traduz a resposta e os erros. Sem
 * este bean no deploy, o Quarkus falha no build (port sem implementação).
 */
@ApplicationScoped
public class SituacaoCadastralAdapter implements SituacaoCadastral {

    private final CadastroClient cliente;

    public SituacaoCadastralAdapter(@RestClient CadastroClient cliente) {
        this.cliente = cliente;
    }

    /** Erro HTTP (WebApplicationException) ou de rede/timeout (ProcessingException) vira erro do domínio. */
    @Override
    public boolean regular(Cpf cpf) {
        try {
            return cliente.consultar(cpf.numero()).regular();
        } catch (WebApplicationException | ProcessingException e) {
            throw new SistemaExternoIndisponivelException("de cadastro", e);
        }
    }
}
