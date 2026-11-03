package com.example.consorcio.domain.exception;

/**
 * Raiz de todas as exceções do domínio. São quatro FAMÍLIAS, cada uma com um significado
 * (e um status HTTP, decidido só no entrypoint):
 *
 * <pre>
 * DomainException
 * ├── DadoInvalidoException              formato inválido (value objects)          → 400
 * ├── RecursoNaoEncontradoException      não existe                                → 404
 * ├── TransicaoEstadoInvalidaException   ação proibida no status atual (State)     → 409
 * └── RegraNegocioException              regra de negócio violada (Specification)  → 422
 * </pre>
 *
 * Cada caso concreto é uma classe própria (ex.: {@code LimitePixExcedidoException}), o que permite
 * {@code catch}/{@code assertThrows} por tipo e deixa o código de erro fixo junto da exceção.
 */
public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    /** Código estável para o cliente da API (não muda quando a mensagem muda). */
    public String getCodigo() {
        return codigo;
    }
}
