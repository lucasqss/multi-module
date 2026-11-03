package com.example.boleto.entrypoint.api.v1;

import com.example.boleto.application.boleto.AlterarBoletoUC;
import com.example.boleto.application.boleto.CancelarBoletoUC;
import com.example.boleto.application.boleto.ConsultarBoletoUC;
import com.example.boleto.application.boleto.EmitirBoletoUC;
import com.example.boleto.application.boleto.ExcluirBoletoUC;
import com.example.boleto.application.boleto.ListarBoletosUC;
import com.example.boleto.application.boleto.NotificarBoletoUC;
import com.example.boleto.application.boleto.ReceberPagamentoBoletoUC;
import com.example.boleto.application.boleto.dto.ListarBoletosInput;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.entrypoint.api.v1.request.AlterarBoletoRequest;
import com.example.boleto.entrypoint.api.v1.request.EmitirBoletoRequest;
import com.example.boleto.entrypoint.api.v1.request.MotivoRequest;
import com.example.boleto.entrypoint.api.v1.request.ReceberPagamentoRequest;
import com.example.boleto.entrypoint.api.v1.response.BoletoResponse;
import com.example.boleto.entrypoint.api.v1.response.PaginaResponse;
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
 * (ex.: {@code POST /{id}/pagamento}), pois são eventos de negócio, não edições de campo.
 */
@Path("/api/v1/boletos")
public class BoletoResource {

    private final EmitirBoletoUC emitir;
    private final ConsultarBoletoUC consultar;
    private final ListarBoletosUC listar;
    private final AlterarBoletoUC alterar;
    private final ExcluirBoletoUC excluir;
    private final NotificarBoletoUC notificar;
    private final ReceberPagamentoBoletoUC receberPagamento;
    private final CancelarBoletoUC cancelar;

    public BoletoResource(EmitirBoletoUC emitir, ConsultarBoletoUC consultar, ListarBoletosUC listar,
                          AlterarBoletoUC alterar, ExcluirBoletoUC excluir, NotificarBoletoUC notificar,
                          ReceberPagamentoBoletoUC receberPagamento, CancelarBoletoUC cancelar) {
        this.emitir = emitir;
        this.consultar = consultar;
        this.listar = listar;
        this.alterar = alterar;
        this.excluir = excluir;
        this.notificar = notificar;
        this.receberPagamento = receberPagamento;
        this.cancelar = cancelar;
    }

    // ============================== CRUD ==============================

    @POST
    public Response emitir(@Valid @NotNull EmitirBoletoRequest request, @Context UriInfo uriInfo) {
        var emitido = BoletoResponse.de(emitir.executar(request.paraInput()));
        var location = uriInfo.getAbsolutePathBuilder().path(emitido.id().toString()).build();
        return Response.created(location).entity(emitido).build();
    }

    @GET
    public PaginaResponse<BoletoResponse> listar(@QueryParam("documentoPagador") String documentoPagador,
                                                 @QueryParam("status") StatusBoleto status,
                                                 @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                 @QueryParam("tamanho") @DefaultValue("20") @Min(1) @Max(100) int tamanho) {
        var resultado = listar.executar(new ListarBoletosInput(documentoPagador, status, pagina, tamanho));
        return PaginaResponse.de(resultado, BoletoResponse::de);
    }

    @GET
    @Path("/{id}")
    public BoletoResponse consultar(@PathParam("id") UUID id) {
        return BoletoResponse.de(consultar.executar(id));
    }

    @PUT
    @Path("/{id}")
    public BoletoResponse alterar(@PathParam("id") UUID id, @Valid @NotNull AlterarBoletoRequest request) {
        return BoletoResponse.de(alterar.executar(request.paraInput(id)));
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        excluir.executar(id);
        return Response.noContent().build();
    }

    // ======================= eventos do ciclo de vida =======================

    /** Simula o envio da notificação de emissão ao pagador (e o reenvio, se já notificado). */
    @POST
    @Path("/{id}/notificacao")
    public BoletoResponse notificar(@PathParam("id") UUID id) {
        return BoletoResponse.de(notificar.executar(id));
    }

    /** Simula o banco avisando que o boleto foi pago. */
    @POST
    @Path("/{id}/pagamento")
    public BoletoResponse receberPagamento(@PathParam("id") UUID id, @Valid @NotNull ReceberPagamentoRequest request) {
        return BoletoResponse.de(receberPagamento.executar(request.paraInput(id)));
    }

    @POST
    @Path("/{id}/cancelamento")
    public BoletoResponse cancelar(@PathParam("id") UUID id, @Valid @NotNull MotivoRequest request) {
        return BoletoResponse.de(cancelar.executar(id, request.motivo()));
    }
}
