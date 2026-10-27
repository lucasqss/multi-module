/**
 * Ponto de extensão do motor: o contrato {@code Segmento<D>} que cada tipo de processo implementa.
 *
 * <p>Segmento novo estende {@code SegmentoBase} (que já faz código, prefixo, tipo, gravar e ler) e escreve só
 * {@code regras()} e {@code avaliar()}. Os segmentos são {@code @Singleton}. Exemplos:
 * {@code segmento.imobiliario.application.service.SegmentoImobiliario} e {@code SegmentoVeicular}.
 */
package com.example.processo.core.motor.application.port.spi;
