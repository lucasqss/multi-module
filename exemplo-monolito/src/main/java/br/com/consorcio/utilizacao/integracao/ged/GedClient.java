package br.com.consorcio.utilizacao.integracao.ged;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ged")
@Path("/documentos")
public interface GedClient {

    record Arquivo(String nome, String mime, String conteudoBase64) {
    }

    record Guardado(String id) {
    }

    @POST
    Guardado guardar(Arquivo arquivo);
}
