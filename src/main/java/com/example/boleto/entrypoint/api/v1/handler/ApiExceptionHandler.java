package com.example.boleto.entrypoint.api.v1.handler;

import com.example.boleto.domain.exception.ConflitoException;
import com.example.boleto.domain.exception.DadoInvalidoException;
import com.example.boleto.domain.exception.DomainException;
import com.example.boleto.domain.exception.RecursoNaoEncontradoException;
import com.example.boleto.domain.exception.RegraNegocioException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Comparator;

/**
 * Traduz TODA exceção para HTTP + {@link ErroResponse}. O domínio nunca sabe o que é um status code.
 *
 * <p>Exceções de domínio são traduzidas pela FAMÍLIA, não pelo caso concreto: uma exceção nova
 * (ex.: {@code BoletoVencidoException}) já sai com o status certo sem mexer aqui.
 */
public class ApiExceptionHandler {

    private static final Logger LOG = Logger.getLogger(ApiExceptionHandler.class);

    @ServerExceptionMapper
    public Response dominio(DomainException e) {
        int status = switch (e) {
            case DadoInvalidoException ignored -> 400;
            case RecursoNaoEncontradoException ignored -> 404;
            case ConflitoException ignored -> 409;
            case RegraNegocioException ignored -> 422;
            default -> 500;
        };
        return resposta(status, new ErroResponse(e.getCodigo(), e.getMessage()));
    }

    /** Bean Validation do contrato HTTP ({@code @NotNull}, {@code @Min}...). */
    @ServerExceptionMapper
    public Response validacao(ConstraintViolationException e) {
        var campos = e.getConstraintViolations().stream()
                .map(v -> new ErroResponse.CampoInvalido(ultimoNo(v), v.getMessage()))
                .sorted(Comparator.comparing(ErroResponse.CampoInvalido::campo))
                .toList();
        return resposta(400, new ErroResponse("REQUISICAO_INVALIDA", "Campos inválidos na requisição", campos));
    }

    /**
     * JSON mal formado ou com tipo errado (ex.: enum inexistente, data inválida).
     * {@code MismatchedInputException} é listada à parte porque o Quarkus já tem um mapper
     * embutido para ela, e o mapper do tipo mais específico vence.
     */
    @ServerExceptionMapper({JsonProcessingException.class, MismatchedInputException.class})
    public Response jsonInvalido(JsonProcessingException e) {
        return resposta(400, new ErroResponse("REQUISICAO_INVALIDA", "Corpo da requisição inválido ou mal formado"));
    }

    /** Erros do próprio JAX-RS: rota/id inexistente (ex.: UUID inválido no path), método não suportado... */
    @ServerExceptionMapper
    public Response http(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        if (status >= 500) {
            return inesperado(e);
        }
        var erro = switch (status) {
            case 404 -> new ErroResponse("RECURSO_NAO_ENCONTRADO", "Recurso não encontrado");
            case 405 -> new ErroResponse("METODO_NAO_PERMITIDO", "Método HTTP não suportado neste recurso");
            case 415 -> new ErroResponse("TIPO_DE_CONTEUDO_NAO_SUPORTADO", "Use Content-Type: application/json");
            default -> new ErroResponse("REQUISICAO_INVALIDA", "Requisição inválida");
        };
        return resposta(status, erro);
    }

    /** Rede de segurança: loga o detalhe e NÃO vaza stack trace para o cliente. */
    @ServerExceptionMapper
    public Response inesperado(Exception e) {
        LOG.error("Erro inesperado", e);
        return resposta(500, new ErroResponse("ERRO_INTERNO", "Erro interno. Tente novamente mais tarde."));
    }

    private static String ultimoNo(ConstraintViolation<?> violacao) {
        String nome = null;
        for (Path.Node no : violacao.getPropertyPath()) {
            nome = no.getName();
        }
        return nome;
    }

    private static Response resposta(int status, ErroResponse erro) {
        return Response.status(status).entity(erro).build();
    }
}
