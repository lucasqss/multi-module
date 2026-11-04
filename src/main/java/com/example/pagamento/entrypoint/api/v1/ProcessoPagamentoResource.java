package com.example.pagamento.entrypoint.api.v1;

import com.example.pagamento.application.processo.AlterarProcessoPagamentoUC;
import com.example.pagamento.application.processo.AprovarProcessoPagamentoUC;
import com.example.pagamento.application.processo.CancelarProcessoPagamentoUC;
import com.example.pagamento.application.processo.ConsultarProcessoPagamentoUC;
import com.example.pagamento.application.processo.CriarProcessoPagamentoUC;
import com.example.pagamento.application.processo.EnviarProcessoParaPagamentoUC;
import com.example.pagamento.application.processo.ExcluirProcessoPagamentoUC;
import com.example.pagamento.application.processo.ListarProcessosPagamentoUC;
import com.example.pagamento.application.processo.ReabrirProcessoPagamentoUC;
import com.example.pagamento.application.processo.RejeitarProcessoPagamentoUC;
import com.example.pagamento.application.processo.RetentarPagamentoUC;
import com.example.pagamento.application.processo.SalvarBoletosDoProcessoUC;
import com.example.pagamento.application.processo.SubmeterProcessoPagamentoUC;
import com.example.pagamento.application.processo.dto.ListarProcessosInput;
import com.example.pagamento.domain.state.StatusProcesso;
import com.example.pagamento.entrypoint.api.v1.request.AlterarProcessoRequest;
import com.example.pagamento.entrypoint.api.v1.request.AprovacaoRequest;
import com.example.pagamento.entrypoint.api.v1.request.CriarProcessoRequest;
import com.example.pagamento.entrypoint.api.v1.request.MotivoRequest;
import com.example.pagamento.entrypoint.api.v1.request.SalvarBoletosRequest;
import com.example.pagamento.entrypoint.api.v1.response.PaginaResponse;
import com.example.pagamento.entrypoint.api.v1.response.ProcessoPagamentoResponse;
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
 * Entrypoint fino: traduz HTTP <-> caso de uso, um endpoint por CASO DE USO (intenção):
 * <ul>
 *   <li>CRUD do rascunho nos verbos HTTP;</li>
 *   <li>cada SEÇÃO como sub-recurso substituído inteiro ({@code PUT /{id}/boletos});</li>
 *   <li>transições como {@code POST /{id}/<substantivo>} ({@code /submissao}, {@code /aprovacoes}, {@code /envio}).</li>
 * </ul>
 * Os FATOS dos sistemas externos (retorno do banco, cancelamento no emissor) não entram por aqui:
 * entram pelos webhooks em {@code entrypoint.webhook.v1}.
 */
@Path("/api/v1/processos-pagamento")
public class ProcessoPagamentoResource {

    private final CriarProcessoPagamentoUC criar;
    private final ConsultarProcessoPagamentoUC consultar;
    private final ListarProcessosPagamentoUC listar;
    private final AlterarProcessoPagamentoUC alterar;
    private final SalvarBoletosDoProcessoUC salvarBoletos;
    private final ExcluirProcessoPagamentoUC excluir;
    private final SubmeterProcessoPagamentoUC submeter;
    private final AprovarProcessoPagamentoUC aprovar;
    private final RejeitarProcessoPagamentoUC rejeitar;
    private final ReabrirProcessoPagamentoUC reabrir;
    private final EnviarProcessoParaPagamentoUC enviar;
    private final RetentarPagamentoUC retentar;
    private final CancelarProcessoPagamentoUC cancelar;

    @SuppressWarnings("java:S107") // um parâmetro por caso de uso: é a natureza do resource
    public ProcessoPagamentoResource(CriarProcessoPagamentoUC criar, ConsultarProcessoPagamentoUC consultar,
                                     ListarProcessosPagamentoUC listar, AlterarProcessoPagamentoUC alterar,
                                     SalvarBoletosDoProcessoUC salvarBoletos, ExcluirProcessoPagamentoUC excluir,
                                     SubmeterProcessoPagamentoUC submeter, AprovarProcessoPagamentoUC aprovar,
                                     RejeitarProcessoPagamentoUC rejeitar, ReabrirProcessoPagamentoUC reabrir,
                                     EnviarProcessoParaPagamentoUC enviar, RetentarPagamentoUC retentar,
                                     CancelarProcessoPagamentoUC cancelar) {
        this.criar = criar;
        this.consultar = consultar;
        this.listar = listar;
        this.alterar = alterar;
        this.salvarBoletos = salvarBoletos;
        this.excluir = excluir;
        this.submeter = submeter;
        this.aprovar = aprovar;
        this.rejeitar = rejeitar;
        this.reabrir = reabrir;
        this.enviar = enviar;
        this.retentar = retentar;
        this.cancelar = cancelar;
    }

    // ============================== CRUD (rascunho) ==============================

    @POST
    public Response criar(@Valid @NotNull CriarProcessoRequest request, @Context UriInfo uriInfo) {
        var criado = ProcessoPagamentoResponse.de(criar.executar(request.solicitante()));
        var location = uriInfo.getAbsolutePathBuilder().path(criado.id().toString()).build();
        return Response.created(location).entity(criado).build();
    }

    @GET
    public PaginaResponse<ProcessoPagamentoResponse> listar(@QueryParam("solicitante") String solicitante,
                                                            @QueryParam("status") StatusProcesso status,
                                                            @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                            @QueryParam("tamanho") @DefaultValue("20") @Min(1) @Max(100) int tamanho) {
        var resultado = listar.executar(new ListarProcessosInput(solicitante, status, pagina, tamanho));
        return PaginaResponse.de(resultado, ProcessoPagamentoResponse::de);
    }

    @GET
    @Path("/{id}")
    public ProcessoPagamentoResponse consultar(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(consultar.executar(id));
    }

    /** Seção "dados gerais" (conta e data de pagamento). */
    @PUT
    @Path("/{id}")
    public ProcessoPagamentoResponse alterar(@PathParam("id") UUID id, @NotNull AlterarProcessoRequest request) {
        return ProcessoPagamentoResponse.de(alterar.executar(request.paraInput(id)));
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        excluir.executar(id);
        return Response.noContent().build();
    }

    // ======================= seções (só em EM_EDICAO) =======================

    @PUT
    @Path("/{id}/boletos")
    public ProcessoPagamentoResponse salvarBoletos(@PathParam("id") UUID id, @Valid @NotNull SalvarBoletosRequest request) {
        return ProcessoPagamentoResponse.de(salvarBoletos.executar(request.paraInput(id)));
    }

    // ======================= fluxo de aprovação =======================

    @POST
    @Path("/{id}/submissao")
    public ProcessoPagamentoResponse submeter(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(submeter.executar(id));
    }

    @POST
    @Path("/{id}/aprovacoes")
    public ProcessoPagamentoResponse aprovar(@PathParam("id") UUID id, @Valid @NotNull AprovacaoRequest request) {
        return ProcessoPagamentoResponse.de(aprovar.executar(id, request.aprovador()));
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

    // ======================= pagamento no banco =======================

    /** Na vida real, disparado por um job na data de pagamento; aqui também pode ser chamado à mão. */
    @POST
    @Path("/{id}/envio")
    public ProcessoPagamentoResponse enviar(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(enviar.executar(id));
    }

    @POST
    @Path("/{id}/retentativa")
    public ProcessoPagamentoResponse retentar(@PathParam("id") UUID id) {
        return ProcessoPagamentoResponse.de(retentar.executar(id));
    }

    @POST
    @Path("/{id}/cancelamento")
    public ProcessoPagamentoResponse cancelar(@PathParam("id") UUID id, @Valid @NotNull MotivoRequest request) {
        return ProcessoPagamentoResponse.de(cancelar.executar(id, request.motivo()));
    }
}
