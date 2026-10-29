package br.com.consorcio.utilizacao.integracao.ged;

import br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo.RepositorioArquivos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Base64;

@ApplicationScoped
public class RepositorioArquivosGed implements RepositorioArquivos {

    @Inject
    @RestClient
    GedClient ged;

    @Override
    public String guardar(String nomeArquivo, String mime, byte[] conteudo) {
        return ged.guardar(new GedClient.Arquivo(nomeArquivo, mime, Base64.getEncoder().encodeToString(conteudo))).id();
    }
}
