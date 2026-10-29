package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Inbox do retorno: a chave única torna o retorno repetido inofensivo. */
@Entity
@Table(name = "pag_retorno")
public class RetornoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_id")
    private Long ordemId;

    @Column(name = "chave_idempotencia")
    private String chaveIdempotencia;

    private String resultado;

    @Column(name = "recebido_em")
    private LocalDateTime recebidoEm;

    protected RetornoPagamento() {
    }

    public RetornoPagamento(Long ordemId, String chaveIdempotencia, String resultado) {
        this.ordemId = ordemId;
        this.chaveIdempotencia = chaveIdempotencia;
        this.resultado = resultado;
        this.recebidoEm = LocalDateTime.now();
    }

    public Long ordemId() { return ordemId; }
}
