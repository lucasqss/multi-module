package br.com.consorcio.utilizacao.rest.parceiro.contemplacao;

import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.math.BigDecimal;

/** O monólito de contemplação usa a garantia por aqui, sem saber que ela mora neste monólito. */
@Path("/parceiro/contemplacao/garantias")
public class GarantiaContemplacaoController {

    public record NovaFianca(String cotaRef, String cpfCnpjFiador, String nomeFiador, BigDecimal valorCobertura) {
    }

    public record Criado(Long id) {
    }

    @Inject
    GarantiaApi garantias;

    @POST
    @Path("/fiancas")
    public Criado registrarFianca(NovaFianca req, @HeaderParam("Idempotency-Key") String chave,
                                  @HeaderParam("X-Usuario") String usuario) {
        return new Criado(garantias.registrarFianca(req.cotaRef(), req.cpfCnpjFiador(), req.nomeFiador(),
                req.valorCobertura(), chave, usuario));
    }

    @GET
    @Path("/cotas/{cotaRef}/cobertura")
    public GarantiaApi.Cobertura cobertura(@PathParam("cotaRef") String cotaRef, @QueryParam("valor") BigDecimal valor) {
        return garantias.cobertura(cotaRef, valor);
    }
}
