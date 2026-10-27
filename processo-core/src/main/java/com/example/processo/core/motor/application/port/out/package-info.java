/**
 * O que o motor precisa de fora, como interfaces (ports): banco ({@code Processos}, {@code Numeracao}), transação
 * ({@code Transacoes}) e sistemas externos ({@code SituacaoCadastral}, {@code GeradorDeDocumentos}).
 *
 * <ul>
 *   <li><b>Banco:</b> implementados pelos repositories Jakarta Data em {@code adapter.outbound.persistence}.</li>
 *   <li><b>Sistemas externos:</b> implementados no módulo {@code integracoes}; o core não conhece REST Client.</li>
 *   <li><b>Testes:</b> cada port tem um fake em {@code Fakes} (test-jar do core). Os de persistência cumprem o mesmo
 *       contrato que o banco ({@code PersistenciaDoProcessoContrato}).</li>
 * </ul>
 *
 * Interface só onde há costura real (ADR 0005): não crie port para algo que tem uma implementação dentro do módulo.
 */
package com.example.processo.core.motor.application.port.out;
