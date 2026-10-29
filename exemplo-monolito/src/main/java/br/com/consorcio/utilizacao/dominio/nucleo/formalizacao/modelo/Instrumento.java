package br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.modelo;

import jakarta.persistence.*;

/** Genérico de propósito: compra e venda com AF hoje; contrato a quitar, prestação de serviço, obra depois. */
@Entity
@Table(name = "ins_instrumento")
public class Instrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    @Column(name = "tipo_instrumento")
    private String tipoInstrumento;

    /** MINUTA, APROVADA, EM_ASSINATURA, ASSINADO, REGISTRADO, CANCELADO */
    private String situacao = "MINUTA";

    protected Instrumento() {
    }

    public Instrumento(Long processoId, String tipoInstrumento) {
        this.processoId = processoId;
        this.tipoInstrumento = tipoInstrumento;
    }

    public Long id() { return id; }

    public Long processoId() { return processoId; }

    public String tipoInstrumento() { return tipoInstrumento; }

    public String situacao() { return situacao; }
}
