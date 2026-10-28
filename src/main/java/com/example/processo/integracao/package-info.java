/**
 * Camada de integração: a lógica de sistemas externos. Um pacote por sistema ({@code cadastro}, {@code pdf}) com o
 * cliente HTTP, a simulação do perfil dev, o formato do sistema externo e uma classe {@code *Integracao}, que é o que o
 * negócio chama. Ela traduz a resposta e os erros: fora do ar vira {@code SistemaExternoIndisponivelException}.
 *
 * <ul>
 *   <li><b>Pode usar:</b> {@code modelo}.</li>
 *   <li><b>Não pode:</b> as outras camadas, nem um sistema usar o outro, nem JPA. O formato do sistema externo não
 *       sai do pacote dele.</li>
 * </ul>
 */
package com.example.processo.integracao;
