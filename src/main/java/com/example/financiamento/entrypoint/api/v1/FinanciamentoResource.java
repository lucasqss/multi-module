package com.example.financiamento.entrypoint.api.v1;

import com.example.financiamento.application.financiamento.AlterarDadosFinanciamentoUC;
import com.example.financiamento.application.financiamento.ConsultarFinanciamentoUC;
import com.example.financiamento.application.financiamento.CriarFinanciamentoUC;
import com.example.financiamento.application.financiamento.ExcluirFinanciamentoUC;
import com.example.financiamento.application.financiamento.ExecutarComandoFinanciamentoUC;
import com.example.financiamento.application.financiamento.InformarGarantiaUC;
import com.example.financiamento.application.financiamento.ListarFinanciamentosUC;
import com.example.financiamento.application.financiamento.SalvarDocumentosUC;
import com.example.financiamento.application.financiamento.comando.CancelarFinanciamento;
import com.example.financiamento.application.financiamento.comando.ContratarFinanciamento;
import com.example.financiamento.application.financiamento.comando.ReenviarRegistro;
import com.example.financiamento.application.financiamento.comando.SubmeterFinanciamento;
import com.example.financiamento.application.financiamento.dto.ListarFinanciamentosInput;
import com.example.financiamento.domain.state.StatusFinanciamento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import com.example.financiamento.entrypoint.api.v1.request.AlterarFinanciamentoRequest;
import com.example.financiamento.entrypoint.api.v1.request.CriarFinanciamentoRequest;
import com.example.financiamento.entrypoint.api.v1.request.GarantiaRequest;
import com.example.financiamento.entrypoint.api.v1.request.MotivoRequest;
import com.example.financiamento.entrypoint.api.v1.request.SalvarDocumentosRequest;
import com.example.financiamento.entrypoint.api.v1.response.FinanciamentoResponse;
import com.example.financiamento.entrypoint.api.v1.response.PaginaResponse;
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
 * Entrypoint fino: traduz HTTP <-> caso de uso.
 * <ul>
 *   <li>CRUD do rascunho nos verbos HTTP; cada SEÇÃO como sub-recurso substituído inteiro
 *       ({@code PUT /{id}/garantia}, {@code PUT /{id}/documentos});</li>
 *   <li>transições como {@code POST /{id}/<substantivo>}: cada uma monta o COMANDO certo e chama o invocador.</li>
 * </ul>
 * O FATO do órgão de registro não entra por aqui: entra pelo webhook em {@code entrypoint.webhook.v1}.
 */
@Path("/api/v1/financiamentos")
public class FinanciamentoResource {

    private final CriarFinanciamentoUC criar;
    private final ConsultarFinanciamentoUC consultar;
    private final ListarFinanciamentosUC listar;
    private final AlterarDadosFinanciamentoUC alterarDados;
    private final InformarGarantiaUC informarGarantia;
    private final SalvarDocumentosUC salvarDocumentos;
    private final ExcluirFinanciamentoUC excluir;
    private final ExecutarComandoFinanciamentoUC executar;

    @SuppressWarnings("java:S107") // um parâmetro por caso de uso: é a natureza do resource
    public FinanciamentoResource(CriarFinanciamentoUC criar, ConsultarFinanciamentoUC consultar, ListarFinanciamentosUC listar,
                                 AlterarDadosFinanciamentoUC alterarDados, InformarGarantiaUC informarGarantia,
                                 SalvarDocumentosUC salvarDocumentos, ExcluirFinanciamentoUC excluir,
                                 ExecutarComandoFinanciamentoUC executar) {
        this.criar = criar;
        this.consultar = consultar;
        this.listar = listar;
        this.alterarDados = alterarDados;
        this.informarGarantia = informarGarantia;
        this.salvarDocumentos = salvarDocumentos;
        this.excluir = excluir;
        this.executar = executar;
    }

    // ============================== CRUD (rascunho) ==============================

    @POST
    public Response criar(@Valid @NotNull CriarFinanciamentoRequest request, @Context UriInfo uriInfo) {
        var criado = FinanciamentoResponse.de(criar.executar(request.tipo()));
        var location = uriInfo.getAbsolutePathBuilder().path(criado.id().toString()).build();
        return Response.created(location).entity(criado).build();
    }

    @GET
    public PaginaResponse<FinanciamentoResponse> listar(@QueryParam("tipo") TipoFinanciamento tipo,
                                                        @QueryParam("status") StatusFinanciamento status,
                                                        @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                        @QueryParam("tamanho") @DefaultValue("20") @Min(1) @Max(100) int tamanho) {
        var resultado = listar.executar(new ListarFinanciamentosInput(tipo, status, pagina, tamanho));
        return PaginaResponse.de(resultado, FinanciamentoResponse::de);
    }

    @GET
    @Path("/{id}")
    public FinanciamentoResponse consultar(@PathParam("id") UUID id) {
        return FinanciamentoResponse.de(consultar.executar(id));
    }

    /** Seção "dados gerais" (proponente, valor solicitado e prazo). */
    @PUT
    @Path("/{id}")
    public FinanciamentoResponse alterar(@PathParam("id") UUID id, @NotNull AlterarFinanciamentoRequest request) {
        return FinanciamentoResponse.de(alterarDados.executar(request.paraInput(id)));
    }

    @PUT
    @Path("/{id}/garantia")
    public FinanciamentoResponse informarGarantia(@PathParam("id") UUID id, @Valid @NotNull GarantiaRequest request) {
        return FinanciamentoResponse.de(informarGarantia.executar(id, request.identificacao()));
    }

    @PUT
    @Path("/{id}/documentos")
    public FinanciamentoResponse salvarDocumentos(@PathParam("id") UUID id, @Valid @NotNull SalvarDocumentosRequest request) {
        return FinanciamentoResponse.de(salvarDocumentos.executar(request.paraInput(id)));
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        excluir.executar(id);
        return Response.noContent().build();
    }

    // ============================== transições (Command) ==============================

    @POST
    @Path("/{id}/submissao")
    public FinanciamentoResponse submeter(@PathParam("id") UUID id) {
        return FinanciamentoResponse.de(executar.executar(id, new SubmeterFinanciamento()));
    }

    @POST
    @Path("/{id}/contratacao")
    public FinanciamentoResponse contratar(@PathParam("id") UUID id) {
        return FinanciamentoResponse.de(executar.executar(id, new ContratarFinanciamento()));
    }

    @POST
    @Path("/{id}/reenvio-registro")
    public FinanciamentoResponse reenviarRegistro(@PathParam("id") UUID id) {
        return FinanciamentoResponse.de(executar.executar(id, new ReenviarRegistro()));
    }

    @POST
    @Path("/{id}/cancelamento")
    public FinanciamentoResponse cancelar(@PathParam("id") UUID id, @Valid @NotNull MotivoRequest request) {
        return FinanciamentoResponse.de(executar.executar(id, new CancelarFinanciamento(request.motivo())));
    }
}
