package br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo;

import java.util.Optional;

/** Porta de leitura do core, implementada em integracao.core. */
public interface ConsultaCore {

    Optional<CotaCore> cota(String cotaRef);
}
