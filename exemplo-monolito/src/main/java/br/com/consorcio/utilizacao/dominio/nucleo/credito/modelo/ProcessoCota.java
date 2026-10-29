package br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vínculo cota-processo com vida própria. */
@Entity
@Table(name = "cre_processo_cota")
public class ProcessoCota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    @Column(name = "cota_ref")
    private String cotaRef;

    @Enumerated(EnumType.STRING)
    private SituacaoVinculo situacao = SituacaoVinculo.RESERVADA;

    @Column(name = "valor_reservado")
    private BigDecimal valorReservado;

    @Column(name = "valor_utilizado")
    private BigDecimal valorUtilizado = BigDecimal.ZERO;

    @Column(name = "snapshot_saldo")
    private BigDecimal snapshotSaldo;

    @Column(name = "snapshot_em")
    private LocalDateTime snapshotEm;

    @Version
    private long versao;

    protected ProcessoCota() {
    }

    /** As regras da cota (contemplada, categoria, titular, saldo) são verificadas aqui, com a fotografia do core. */
    public static ProcessoCota reservar(Long processoId, CotaCore cota, BigDecimal valor, String categoria, String cpfTitular) {
        RegraNegocioException.exigir(cota.contemplada(), "COTA_NAO_CONTEMPLADA", "Cota " + cota.cotaRef() + " não está contemplada");
        RegraNegocioException.exigir(cota.categoria().equals(categoria), "MOT-011",
                "Cota de categoria " + cota.categoria() + " não pode adquirir " + categoria);
        RegraNegocioException.exigir(cota.cpfTitular().equals(cpfTitular), "COTA_DE_OUTRO_TITULAR",
                "Cota " + cota.cotaRef() + " não pertence ao titular do processo");
        RegraNegocioException.exigir(cota.saldoUtilizavel().compareTo(valor) >= 0, "SALDO_INSUFICIENTE",
                "Saldo da cota " + cota.saldoUtilizavel() + " menor que " + valor);
        ProcessoCota v = new ProcessoCota();
        v.processoId = processoId;
        v.cotaRef = cota.cotaRef();
        v.valorReservado = valor;
        v.snapshotSaldo = cota.saldoUtilizavel();
        v.snapshotEm = LocalDateTime.now();
        return v;
    }

    public void registrarUtilizacao(BigDecimal valor) {
        RegraNegocioException.exigir(situacao == SituacaoVinculo.RESERVADA, "VINCULO_ENCERRADO", "Cota " + cotaRef + " está " + situacao);
        RegraNegocioException.exigir(valor.compareTo(disponivel()) <= 0, "ORIGEM_ACIMA_DO_RESERVADO",
                "Cota " + cotaRef + " tem " + disponivel() + " disponível");
        valorUtilizado = valorUtilizado.add(valor);
    }

    public void encerrar(SituacaoVinculo destino) {
        RegraNegocioException.exigir(destino != SituacaoVinculo.RESERVADA, "ENCERRAMENTO_INVALIDO", "Destino inválido");
        situacao = destino;
    }

    public BigDecimal disponivel() {
        return valorReservado.subtract(valorUtilizado);
    }

    public Long id() { return id; }

    public Long processoId() { return processoId; }

    public String cotaRef() { return cotaRef; }

    public SituacaoVinculo situacao() { return situacao; }

    public BigDecimal valorReservado() { return valorReservado; }

    public BigDecimal valorUtilizado() { return valorUtilizado; }
}
