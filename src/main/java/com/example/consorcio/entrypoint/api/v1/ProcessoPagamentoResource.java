package com.example.consorcio.entrypoint.api.v1;

import com.example.consorcio.application.processopagamento.AlterarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.CancelarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.ConfirmarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.ConsultarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.CriarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.EnviarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.ExcluirProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.ListarProcessosPagamentoUC;
import com.example.consorcio.application.processopagamento.ReabrirProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.RejeitarProcessoPagamentoUC;
import com.example.consorcio.application.processopagamento.dto.ListarProcessosPagamentoInput;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.entrypoint.api.v1.request.AlterarProcessoPagamentoRequest;
import com.example.consorcio.entrypoint.api.v1.request.CriarProcessoPagamentoRequest;
import com.example.consorcio.entrypoint.api.v1.request.MotivoRequest;
import com.example.consorcio.entrypoint.api.v1.response.PaginaResponse;
import com.example.consorcio.entrypoint.api.v1.response.ProcessoPagamentoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.UUID;

/**
 * Entrypoint fino: traduz HTTP <-> caso de uso. Nenhuma regra de negócio aqui.
 *
 * <p>CRUD em verbos HTTP; transições de estado como sub-recursos com POST
 * (ex.: {@code POST /{id}/cancelamento}), pois são ações de negócio, não edições de campo.
 */
@Path("/api/v1/processos-pagamento")
public class ProcessoPagamentoResource {

    private final CriarProcessoPagamentoUC criar;
    private final ConsultarProcessoPagamentoUC consultar;
    private final ListarProcessosPagamentoUC listar;
    private final AlterarProcessoPagamentoUC alterar;
    private final ExcluirProcessoPagamentoUC excluir;
    private final EnviarProcessoPagamentoUC enviar;
    private final ConfirmarProcessoPagamentoUC confirmar;
    private final RejeitarProcessoPagamentoUC rejeitar;
    private final ReabrirProcessoPagamentoUC reabrir;
    private final CancelarProcessoPagamentoUC cancelar;

    public ProcessoPagamentoResource(CriarProcessoPagamentoUC criar, ConsultarProcessoPagamentoUC consultar,
                                     ListarProcessosPagamentoUC listar, AlterarProcessoPagamentoUC alterar,
                                     ExcluirProcessoPagamentoUC excluir, EnviarProcessoPagamentoUC enviar,
                                     ConfirmarProcessoPagamentoUC confirmar, RejeitarProcessoPagamentoUC rejeitar,
                                     ReabrirProcessoPagamentoUC reabrir, CancelarProcessoPagamentoUC cancelar) {
        this.criar = criar;
        this.consultar = consultar;
        this.listar = listar;
        this.alterar = alterar;
        this.excluir = excluir;
        this.enviar = enviar;
        this.confirmar = confirmar;
        this.rejeitar = rejeitar;
        this.reabrir = reabrir;
        this.cancelar = cancelar;
    }

    // ============================== CRUD ==============================

    @POST
    public Response criar(@Valid @NotNull CriarProcessoPagamentoRequest request, @Context UriInfo uriInfo) {
        var criado = ProcessoPagamentoResponse.de(criar.executar(request.paraInput()));
        var location = uriInfo.getAbsolutePathBuilder().path(criado.id().toString()).build();
        return Response.created(location).entity(criado).build();
    }

    @GET
    public PaginaResponse<ProcessoPagamentoResponse> listar(@QueryParam("grupo") Integer grupo,
                                                  @QueryParam("cota") Integer cota,
                                                  @QueryParam("status") StatusProcessoPagamento status,
                                                  @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                  @QueryParam("tamanho") @DefaultValue("20") @Min(1) @Max(100) int tamanho) {
        var resultado = listar.executar(new ListarProcessosPagamentoInput(grupo, cota, status, pagina, tamanho));
        return PaginaResponse.de(resultado, ProcessoPagamentoResponse::de);
    }

    @GET
    @Path("/{id}")
    public ProcessoPagamentoResponse consultar(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(consultar.executar(id));
    }

    @PUT
    @Path("/{id}")
    public ProcessoPagamentoResponse alterar(@PathParam("id") UUID id,
                                             @Valid @NotNull AlterarProcessoPagamentoRequest request) {
        return ProcessoPagamentoResponse.de(alterar.executar(request.paraInput(id)));
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        excluir.executar(id);
        return Response.noContent().build();
    }

    // ======================= transições de estado =======================

    @POST
    @Path("/{id}/envio")
    public ProcessoPagamentoResponse enviar(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(enviar.executar(id));
    }

    @POST
    @Path("/{id}/confirmacao")
    public ProcessoPagamentoResponse confirmar(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(confirmar.executar(id));
    }

    @POST
    @Path("/{id}/rejeicao")
    public ProcessoPagamentoResponse rejeitar(@PathParam("id") UUID id, @Valid @NotNull MotivoRequest request) {
        return ProcessoPagamentoResponse.de(rejeitar.executar(id, request.motivo()));
    }

    @POST
    @Path("/{id}/reabertura")
    public ProcessoPagamentoResponse reabrir(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(reabrir.executar(id));
    }

    @POST
    @Path("/{id}/cancelamento")
    public ProcessoPagamentoResponse cancelar(@PathParam("id") UUID id, @Valid @NotNull MotivoRequest request) {
        return ProcessoPagamentoResponse.de(cancelar.executar(id, request.motivo()));
    }
}
