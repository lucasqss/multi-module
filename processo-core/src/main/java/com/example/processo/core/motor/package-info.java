/**
 * O motor único: o mesmo fluxo de criação e o mesmo ciclo de vida para todos os segmentos. Ponto de entrada:
 * {@code application.service.MotorDeProcessamento}.
 *
 * <p>Estrutura hexagonal, com a dependência sempre para dentro ({@code adapter → application → domain}):
 * <ul>
 *   <li>{@code domain}: Java puro (modelo, máquina de estados, regras, exceções);</li>
 *   <li>{@code application.port.out}: o que o motor precisa de fora (banco, sistemas externos, transação);</li>
 *   <li>{@code application.port.spi}: o contrato que cada segmento implementa;</li>
 *   <li>{@code application.service}: o motor;</li>
 *   <li>{@code adapter}: implementações de infraestrutura dos ports (repositories, transação);</li>
 *   <li>{@code config}: producers CDI.</li>
 * </ul>
 *
 * <b>Não pode:</b> conhecer qualquer {@code segmento.*} (o motor descobre os segmentos pelo CDI). Decisões: ADR 0002,
 * 0005, 0006 e 0007.
 */
package com.example.processo.core.motor;
