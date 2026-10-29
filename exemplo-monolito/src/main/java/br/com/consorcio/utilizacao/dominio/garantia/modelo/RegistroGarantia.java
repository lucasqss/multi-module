package br.com.consorcio.utilizacao.dominio.garantia.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.persistence.*;

import java.time.LocalDate;

/** Protocolo e registro no órgão (cartório de RI, Detran/SNG, RTD). */
@Entity
@Table(name = "gar_registro")
public class RegistroGarantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "garantia_id")
    private Garantia garantia;

    private String orgao;

    private String protocolo;

    @Column(name = "numero_registro")
    private String numeroRegistro;

    @Column(name = "registrado_em")
    private LocalDate registradoEm;

    @Column(name = "conferido_por")
    private String conferidoPor;

    protected RegistroGarantia() {
    }

    RegistroGarantia(Garantia garantia, String orgao, String protocolo) {
        this.garantia = garantia;
        this.orgao = orgao;
        this.protocolo = protocolo;
    }

    void registrar(String numero, LocalDate em) {
        this.numeroRegistro = numero;
        this.registradoEm = em;
    }

    void conferir(String conferente) {
        RegraNegocioException.exigir(numeroRegistro != null, "REGISTRO_PENDENTE", "Garantia ainda não foi registrada");
        this.conferidoPor = conferente;
    }
}
