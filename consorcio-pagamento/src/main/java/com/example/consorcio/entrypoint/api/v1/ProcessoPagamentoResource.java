package com.example.consorcio.entrypoint.api.v1;

import com.example.consorcio.application.pagamento.ConsultarProcessoPagamentoUC;
import com.example.consorcio.application.pagamento.CriarProcessoPagamentoUC;
import com.example.consorcio.application.pagamento.dto.ProcessoPagamentoOutput;
import com.example.consorcio.entrypoint.api.v1.request.CriarProcessoPagamentoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

/** Entrypoint fino: traduz HTTP <-> caso de uso. Nenhuma regra de negócio aqui. */
@Path("/api/v1/processos-pagamento")
public class ProcessoPagamentoResource {

    private final CriarProcessoPagamentoUC criarProcesso;
    private final ConsultarProcessoPagamentoUC consultarProcesso;

    public ProcessoPagamentoResource(CriarProcessoPagamentoUC criarProcesso,
                                     ConsultarProcessoPagamentoUC consultarProcesso) {
        this.criarProcesso = criarProcesso;
        this.consultarProcesso = consultarProcesso;
    }

    @POST
    public Response criar(@Valid @NotNull CriarProcessoPagamentoRequest request, @Context UriInfo uriInfo) {
        ProcessoPagamentoOutput criado = criarProcesso.executar(request.paraInput());
        var location = uriInfo.getAbsolutePathBuilder().path(criado.id()).build();
        return Response.created(location).entity(criado).build();
    }

    @GET
    @Path("/{id}")
    public ProcessoPagamentoOutput consultar(@PathParam("id") String id) {
        return consultarProcesso.executar(id);
    }
}
