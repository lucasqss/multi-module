package br.com.consorcio.utilizacao.compartilhado;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado: " + id);
    }
}
