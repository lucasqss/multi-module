package br.com.consorcio.utilizacao.integracao.outbox;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Intenção de falar com um sistema externo, gravada na mesma transação do fato de negócio. */
@Entity
@Table(name = "int_outbox")
public class MensagemOutbox {

    public enum Situacao { PENDENTE, ENVIADA, FALHOU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    /** Enviada ao externo para que a repetição seja inofensiva. */
    @Column(name = "chave_idempotencia")
    private String chaveIdempotencia;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private Situacao situacao = Situacao.PENDENTE;

    private int tentativas;

    @Column(name = "ultimo_erro")
    private String ultimoErro;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    protected MensagemOutbox() {
    }

    MensagemOutbox(String tipo, Long referenciaId, String chaveIdempotencia, String payload) {
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.chaveIdempotencia = chaveIdempotencia;
        this.payload = payload;
    }

    void marcarEnviada() {
        situacao = Situacao.ENVIADA;
        enviadoEm = LocalDateTime.now();
    }

    void registrarFalha(String erro, int maxTentativas) {
        tentativas++;
        ultimoErro = erro.length() <= 1000 ? erro : erro.substring(0, 1000);
        if (tentativas >= maxTentativas) {
            situacao = Situacao.FALHOU;
        }
    }

    public Long id() { return id; }

    public String tipo() { return tipo; }

    public String chaveIdempotencia() { return chaveIdempotencia; }

    public String payload() { return payload; }
}
