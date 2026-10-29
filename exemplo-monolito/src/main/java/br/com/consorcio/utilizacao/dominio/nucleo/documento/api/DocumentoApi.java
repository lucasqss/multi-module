package br.com.consorcio.utilizacao.dominio.nucleo.documento.api;

public interface DocumentoApi {

    /** Dono: o objeto ou a participação informados; se nenhum, o próprio processo. */
    record EnviarDocumento(Long processoId, String tipoDocumento, String nomeArquivo, String mime, byte[] conteudo,
                           Long objetoId, Long participacaoId) {
    }

    Long enviar(EnviarDocumento cmd);
}
