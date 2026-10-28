package com.example.processo.integracao.cadastro;

import com.example.processo.modelo.SistemaExternoIndisponivelException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * O que o negócio chama para falar com o sistema de cadastro: chama o cliente HTTP e traduz a resposta e os erros. O
 * formato do sistema externo ({@link RespostaCadastro}) e as exceções do REST Client não passam daqui.
 */
@ApplicationScoped
public class CadastroIntegracao {

    private final CadastroClient cliente;

    public CadastroIntegracao(@RestClient CadastroClient cliente) {
        this.cliente = cliente;
    }

    /** Erro HTTP (WebApplicationException) ou de rede/timeout (ProcessingException) vira indisponibilidade. */
    public boolean regular(String cpf) {
        try {
            return cliente.consultar(cpf).regular();
        } catch (WebApplicationException | ProcessingException e) {
            throw new SistemaExternoIndisponivelException("de cadastro", e);
        }
    }
}
