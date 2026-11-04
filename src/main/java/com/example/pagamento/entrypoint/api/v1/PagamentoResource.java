package com.example.pagamento.entrypoint.api.v1;

import com.example.pagamento.application.pagamento.AlterarDadosPagamentoUC;
import com.example.pagamento.application.pagamento.ConsultarPagamentoUC;
import com.example.pagamento.application.pagamento.CriarPagamentoUC;
import com.example.pagamento.application.pagamento.ExcluirPagamentoUC;
import com.example.pagamento.application.pagamento.ExecutarComandoPagamentoUC;
import com.example.pagamento.application.pagamento.ListarPagamentosUC;
import com.example.pagamento.application.pagamento.SalvarBoletosDoPagamentoUC;
import com.example.pagamento.application.pagamento.comando.CancelarPagamento;
import com.example.pagamento.application.pagamento.comando.EnviarAoBanco;
import com.example.pagamento.application.pagamento.comando.ReenviarAoBanco;
import com.example.pagamento.application.pagamento.comando.SubmeterPagamento;
import com.example.pagamento.application.pagamento.dto.ListarPagamentosInput;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.entrypoint.api.v1.request.AlterarPagamentoRequest;
import com.example.pagamento.entrypoint.api.v1.request.CriarPagamentoRequest;
import com.example.pagamento.entrypoint.api.v1.request.MotivoRequest;
import com.example.pagamento.entrypoint.api.v1.request.SalvarBoletosRequest;
import com.example.pagamento.entrypoint.api.v1.response.PagamentoResponse;
import com.example.pagamento.entrypoint.api.v1.response.PaginaResponse;
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
 *   <li>CRUD do rascunho nos verbos HTTP; cada SEÇÃO como sub-recurso substituído inteiro ({@code PUT /{id}/boletos});</li>
 *   <li>transições como {@code POST /{id}/<substantivo>}: cada uma monta o COMANDO certo e chama o invocador.</li>
 * </ul>
 * O FATO do banco (retorno do pagamento) não entra por aqui: entra pelo webhook em {@code entrypoint.webhook.v1}.
 */
@Path("/api/v1/pagamentos")
public class PagamentoResource {

    private final CriarPagamentoUC criar;
    private final ConsultarPagamentoUC consultar;
    private final ListarPagamentosUC listar;
    private final AlterarDadosPagamentoUC alterarDados;
    private final SalvarBoletosDoPagamentoUC salvarBoletos;
    private final ExcluirPagamentoUC excluir;
    private final ExecutarComandoPagamentoUC executar;

    public PagamentoResource(CriarPagamentoUC criar, ConsultarPagamentoUC consultar, ListarPagamentosUC listar,
                             AlterarDadosPagamentoUC alterarDados, SalvarBoletosDoPagamentoUC salvarBoletos,
                             ExcluirPagamentoUC excluir, ExecutarComandoPagamentoUC executar) {
        this.criar = criar;
        this.consultar = consultar;
        this.listar = listar;
        this.alterarDados = alterarDados;
        this.salvarBoletos = salvarBoletos;
        this.excluir = excluir;
        this.executar = executar;
    }

    // ============================== CRUD (rascunho) ==============================

    @POST
    public Response criar(@Valid @NotNull CriarPagamentoRequest request, @Context UriInfo uriInfo) {
        var criado = PagamentoResponse.de(criar.executar(request.modalidade()));
        var location = uriInfo.getAbsolutePathBuilder().path(criado.id().toString()).build();
        return Response.created(location).entity(criado).build();
    }

    @GET
    public PaginaResponse<PagamentoResponse> listar(@QueryParam("modalidade") Modalidade modalidade,
                                                    @QueryParam("status") StatusPagamento status,
                                                    @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                    @QueryParam("tamanho") @DefaultValue("20") @Min(1) @Max(100) int tamanho) {
        var resultado = listar.executar(new ListarPagamentosInput(modalidade, status, pagina, tamanho));
        return PaginaResponse.de(resultado, PagamentoResponse::de);
    }

    @GET
    @Path("/{id}")
    public PagamentoResponse consultar(@PathParam("id") UUID id) {
        return PagamentoResponse.de(consultar.executar(id));
    }

    /** Seção "dados gerais" (conta e data de pagamento). */
    @PUT
    @Path("/{id}")
    public PagamentoResponse alterar(@PathParam("id") UUID id, @NotNull AlterarPagamentoRequest request) {
        return PagamentoResponse.de(alterarDados.executar(request.paraInput(id)));
    }

    @PUT
    @Path("/{id}/boletos")
    public PagamentoResponse salvarBoletos(@PathParam("id") UUID id, @Valid @NotNull SalvarBoletosRequest request) {
        return PagamentoResponse.de(salvarBoletos.executar(request.paraInput(id)));
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
    public PagamentoResponse submeter(@PathParam("id") UUID id) {
        return PagamentoResponse.de(executar.executar(id, new SubmeterPagamento()));
    }

    /** Na vida real, disparado por um job na data de pagamento; aqui também pode ser chamado à mão. */
    @POST
    @Path("/{id}/envio")
    public PagamentoResponse enviar(@PathParam("id") UUID id) {
        return PagamentoResponse.de(executar.executar(id, new EnviarAoBanco()));
    }

    @POST
    @Path("/{id}/reenvio")
    public PagamentoResponse reenviar(@PathParam("id") UUID id) {
        return PagamentoResponse.de(executar.executar(id, new ReenviarAoBanco()));
    }

    @POST
    @Path("/{id}/cancelamento")
    public PagamentoResponse cancelar(@PathParam("id") UUID id, @Valid @NotNull MotivoRequest request) {
        return PagamentoResponse.de(executar.executar(id, new CancelarPagamento(request.motivo())));
    }
}
