package br.com.consorcio.utilizacao.dominio.nucleo.credito.api;

import java.math.BigDecimal;
import java.util.List;

/** Crédito do processo: cotas reservadas (o core é dono da cota) e recursos que não são carta. */
public interface CreditoApi {

    record CotaReservada(Long id, Long processoId, String cotaRef, String situacao,
                         BigDecimal reservado, BigDecimal utilizado, BigDecimal disponivel) {
    }

    enum Encerramento { UTILIZADAS, LIBERADAS }

    CotaReservada reservar(Long processoId, String cotaRef, BigDecimal valor, String categoria, String cpfTitular);

    List<CotaReservada> reservadas(Long processoId);

    List<CotaReservada> doProcesso(Long processoId);

    CotaReservada vinculo(Long vinculoId);

    BigDecimal totalReservado(Long processoId);

    BigDecimal recursosComprovados(Long processoId);

    /** Relê o saldo no core antes de comprometer dinheiro: o snapshot da reserva pode estar velho. */
    void conferirSaldoNoCore(Long vinculoId, BigDecimal valor);

    void registrarUtilizacao(Long vinculoId, BigDecimal valor);

    void encerrarReservas(Long processoId, Encerramento como);
}
