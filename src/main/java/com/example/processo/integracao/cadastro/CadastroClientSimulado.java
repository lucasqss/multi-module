package com.example.processo.integracao.cadastro;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Simulação do sistema de cadastro para o perfil dev, para rodar a aplicação sem o sistema externo. Responde
 * sempre REGULAR, sem rede.
 *
 * <ul>
 *   <li>{@code @IfBuildProfile("dev")}: o bean só existe no build de dev. No jar de produção (e nos ITs, que
 *       rodam contra ele) não há simulação: vale o REST Client.</li>
 *   <li>{@code @RestClient} + {@code @Alternative @Priority}: mesmo tipo e qualificador do REST Client gerado
 *       pelo Quarkus, com prioridade sobre ele. Quem injeta ({@link CadastroIntegracao}) não sabe qual
 *       recebeu.</li>
 * </ul>
 */
@IfBuildProfile("dev")
@Alternative
@Priority(1)
@RestClient
@ApplicationScoped
public class CadastroClientSimulado implements CadastroClient {

    @Override
    public RespostaCadastro consultar(String documento) {
        return new RespostaCadastro(documento, RespostaCadastro.REGULAR);
    }
}
