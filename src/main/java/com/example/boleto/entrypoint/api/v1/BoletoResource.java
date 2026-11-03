package com.example.boleto.entrypoint.api.v1;

import com.example.boleto.application.boleto.AlterarBoletoUC;
import com.example.boleto.application.boleto.CancelarBoletoUC;
import com.example.boleto.application.boleto.ConsultarBoletoUC;
import com.example.boleto.application.boleto.CriarBoletoUC;
import com.example.boleto.application.boleto.EmitirBoletoUC;
import com.example.boleto.application.boleto.ExcluirBoletoUC;
import com.example.boleto.application.boleto.ListarBoletosUC;
import com.example.boleto.application.boleto.NotificarBoletoUC;
import com.example.boleto.application.boleto.ReceberPagamentoBoletoUC;
import com.example.boleto.application.boleto.SalvarDestinatariosBoletoUC;
import com.example.boleto.application.boleto.SalvarItensBoletoUC;
import com.example.boleto.application.boleto.dto.ListarBoletosInput;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.entrypoint.api.v1.request.AlterarBoletoRequest;
import com.example.boleto.entrypoint.api.v1.request.CriarBoletoRequest;
import com.example.boleto.entrypoint.api.v1.request.MotivoRequest;
import com.example.boleto.entrypoint.api.v1.request.ReceberPagamentoRequest;
import com.example.boleto.entrypoint.api.v1.request.SalvarDestinatariosBoletoRequest;
import com.example.boleto.entrypoint.api.v1.request.SalvarItensBoletoRequest;
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
 * <p>Um endpoint por CASO DE USO (intenção do usuário):
 * <ul>
 *   <li>CRUD do rascunho nos verbos HTTP ({@code POST}, {@code GET}, {@code PUT /{id}}, {@code DELETE /{id}});</li>
 *   <li>cada SEÇÃO do boleto como sub-recurso substituído inteiro ({@code PUT /{id}/itens});</li>
 *   <li>transições/eventos de negócio como {@code POST /{id}/<substantivo>} ({@code /emissao}, {@code /pagamento}).</li>
 * </ul>
 */
@Path("/api/v1/boletos")
public class BoletoResource {

    private final CriarBoletoUC criar;
    private final ConsultarBoletoUC consultar;
    private final ListarBoletosUC listar;
    private final AlterarBoletoUC alterar;
    private final SalvarItensBoletoUC salvarItens;
    private final SalvarDestinatariosBoletoUC salvarDestinatarios;
    private final ExcluirBoletoUC excluir;
    private final EmitirBoletoUC emitir;
    private final NotificarBoletoUC notificar;
    private final ReceberPagamentoBoletoUC receberPagamento;
    private final CancelarBoletoUC cancelar;

    public BoletoResource(CriarBoletoUC criar, ConsultarBoletoUC consultar, ListarBoletosUC listar,
                          AlterarBoletoUC alterar, SalvarItensBoletoUC salvarItens,
                          SalvarDestinatariosBoletoUC salvarDestinatarios, ExcluirBoletoUC excluir,
                          EmitirBoletoUC emitir, NotificarBoletoUC notificar,
                          ReceberPagamentoBoletoUC receberPagamento, CancelarBoletoUC cancelar) {
        this.criar = criar;
        this.salvarItens = salvarItens;
        this.salvarDestinatarios = salvarDestinatarios;
        this.emitir = emitir;
        this.consultar = consultar;
        this.listar = listar;
        this.alterar = alterar;
        this.excluir = excluir;
        this.notificar = notificar;
        this.receberPagamento = receberPagamento;
        this.cancelar = cancelar;
    }

    // ============================== CRUD (rascunho) ==============================

    /** Cria o boleto em EM_EDICAO (rascunho), só com pagador e vencimento. */
    @POST
    public Response criar(@Valid @NotNull CriarBoletoRequest request, @Context UriInfo uriInfo) {
        var criado = BoletoResponse.de(criar.executar(request.paraInput()));
        var location = uriInfo.getAbsolutePathBuilder().path(criado.id().toString()).build();
        return Response.created(location).entity(criado).build();
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

    // ======================= seções (só em EM_EDICAO) =======================

    @PUT
    @Path("/{id}/itens")
    public BoletoResponse salvarItens(@PathParam("id") UUID id, @Valid @NotNull SalvarItensBoletoRequest request) {
        return BoletoResponse.de(salvarItens.executar(request.paraInput(id)));
    }

    @PUT
    @Path("/{id}/destinatarios")
    public BoletoResponse salvarDestinatarios(@PathParam("id") UUID id,
                                              @Valid @NotNull SalvarDestinatariosBoletoRequest request) {
        return BoletoResponse.de(salvarDestinatarios.executar(request.paraInput(id)));
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        excluir.executar(id);
        return Response.noContent().build();
    }

    // ======================= eventos do ciclo de vida =======================

    /** Finaliza a edição: exige completude e simula a criação do boleto (gera a linha digitável). */
    @POST
    @Path("/{id}/emissao")
    public BoletoResponse emitir(@PathParam("id") UUID id) {
        return BoletoResponse.de(emitir.executar(id));
    }

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
