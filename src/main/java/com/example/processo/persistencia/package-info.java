/**
 * Camada de persistência: a lógica de banco. {@code repositorio} grava e lê entidades para o negócio;
 * {@code consulta} projeta direto para as telas (somente leitura). Tudo é repository Jakarta Data: a implementação é
 * gerada na compilação, que também valida o HQL. As migrations ficam em {@code resources/db/migration}.
 *
 * <ul>
 *   <li><b>Pode usar:</b> {@code modelo}.</li>
 *   <li><b>Não pode:</b> as outras camadas, nem ter regra de negócio.</li>
 * </ul>
 */
package com.example.processo.persistencia;
