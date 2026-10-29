package br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo;

/** Porta para o GED corporativo, implementada em integracao.ged. Aqui só guardamos o id devolvido. */
public interface RepositorioArquivos {

    String guardar(String nomeArquivo, String mime, byte[] conteudo);
}
