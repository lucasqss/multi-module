package com.example.processo.core.motor.domain;

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
}
