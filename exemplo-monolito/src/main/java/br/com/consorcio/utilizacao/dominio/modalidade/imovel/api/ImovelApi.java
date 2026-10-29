package br.com.consorcio.utilizacao.dominio.modalidade.imovel.api;

import java.math.BigDecimal;

/** O que só o imóvel sabe fazer. O resto do ciclo de vida é do núcleo (ProcessoApi). */
public interface ImovelApi {

    record AbrirProcessoImovel(String cpfTitular, String nomeTitular, String tipoOperacao, String matricula,
                               String cartorioRi, String inscricaoIptu, String uf, String municipio,
                               boolean construcaoAverbada, BigDecimal valorNegociado,
                               String contatoNome, String contatoEmail, String contatoTelefone) {
    }

    Long abrir(AbrirProcessoImovel cmd, String ator);
}
