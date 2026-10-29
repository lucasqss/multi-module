package br.com.consorcio.utilizacao.rest.backoffice.abertura;

import java.math.BigDecimal;

/** Formato das telas de abertura. Muda com a tela; o domínio não fica sabendo. */
public final class AberturaDto {

    private AberturaDto() {
    }

    public record ContatoAcesso(String nome, String email, String telefone) {
    }

    public record NovoProcessoImovel(String cpfTitular, String nomeTitular, String tipoOperacao, String matricula,
                                     String cartorioRi, String inscricaoIptu, String uf, String municipio,
                                     boolean construcaoAverbada, BigDecimal valorNegociado, ContatoAcesso contatoAcesso) {
    }

    public record NovoParticipante(String tipo, String cpfCnpj, String nome, String papel) {
    }

    public record NovaConta(String banco, String agencia, String conta, String chavePix, boolean titularidadeVerificada) {
    }

    public record NovaReserva(String cotaRef, BigDecimal valor) {
    }

    public record Criado(Long id) {
    }

    public record CotaReservada(Long id, String cotaRef, String situacao, BigDecimal reservado) {
    }
}
