package com.example.processo.core.motor.application.port.spi;

import com.example.processo.core.motor.domain.model.Avaliacao;
import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.rule.Especificacao;

import java.util.Optional;
import java.util.UUID;

/**
 * Ponto de extensão do motor: tudo o que muda de um segmento para outro. Cada segmento é um módulo com uma
 * implementação desta interface (bean CDI); o motor descobre todas e escolhe pela classe dos dados. Segmento novo
 * não altera o motor.
 */
public interface Segmento<D extends DadosDoSegmento> {

    /** Código gravado como discriminador (PROCESSO.SEGMENTO). */
    String codigo();

    /** Prefixo do número do processo. */
    String prefixo();

    Class<D> tipoDosDados();

    /** Regras próprias do segmento; o motor só as aplica depois das {@link RegrasComuns}. */
    Especificacao<Solicitacao<D>> regras();

    /** Grava o processo inteiro: dados comuns e do segmento. */
    void salvar(Processo<D> processo);

    /** Lê o processo inteiro (dados comuns e do segmento). */
    Optional<Processo<D>> carregar(UUID id);

    /**
     * A decisão própria do segmento no processamento. Roda fora de transação: pode consultar sistema externo.
     * Os estados e as transições são do motor (padrão State em {@code domain.state}); o segmento só decide.
     */
    Avaliacao avaliar(Processo<D> processo);
}
