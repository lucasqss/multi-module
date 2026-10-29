package br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo;

import java.math.BigDecimal;

/** Fotografia da cota lida do core. Nunca gravada como verdade: o core é o dono. */
public record CotaCore(String cotaRef, String categoria, String regimeNormativo, String cpfTitular,
                       boolean contemplada, BigDecimal saldoUtilizavel) {
}
