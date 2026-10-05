package com.example.modelo.core;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuração do core. O valor padrão fica em {@code @WithDefault}, não num {@code application.properties}
 * deste módulo: esse arquivo também seria lido e disputaria com o do app no mesmo ordinal (250), e quem
 * vence passa a depender da ordem do classpath. O app sobrescreve o padrão no seu application.properties.
 */
@ConfigMapping(prefix = "modelo.saudacao")
public interface SaudacaoConfig {

    @WithDefault("Olá")
    String prefixo();
}
