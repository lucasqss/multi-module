package br.com.consorcio.utilizacao.compartilhado;

/** Violação de regra de negócio. O REST devolve 409 com o código. */
public class RegraNegocioException extends RuntimeException {

    private final String codigo;

    public RegraNegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public static void exigir(boolean condicao, String codigo, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(codigo, mensagem);
        }
    }
}
