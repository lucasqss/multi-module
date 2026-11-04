package com.example.pagamento.adapter.emissor;

import com.example.pagamento.domain.exception.BoletoIndisponivelNoEmissorException;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.LinhaDigitavel;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Integração de SAÍDA com o SISTEMA EMISSOR de boletos (dono do ciclo de vida do boleto).
 * Consulta um boleto pela linha digitável e o traduz para o nosso {@link BoletoAPagar}.
 * O sentido de ENTRADA (o emissor nos avisa de um cancelamento) é o
 * {@code entrypoint.webhook.v1.EmissorWebhookResource}.
 *
 * <p>SIMULAÇÃO: na vida real seria um REST client. Aqui, o "emissor" decodifica a própria linha
 * (layout FAKE, não FEBRABAN): {@code 9} + 3 dígitos do emissor + 25 dígitos de id + vencimento
 * {@code yyyyMMdd} + valor em centavos (10 dígitos). Linha começando com {@code 0} = boleto
 * indisponível (inexistente, cancelado ou já pago).
 */
@ApplicationScoped
public class EmissorBoletos {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyyMMdd");

    public BoletoAPagar consultar(LinhaDigitavel linha) {
        var v = linha.valor();
        if (v.startsWith("0")) {
            throw new BoletoIndisponivelNoEmissorException(linha);
        }
        try {
            var id = "EMI-" + v.substring(4, 29).replaceFirst("^0+(?!$)", "");
            var vencimento = LocalDate.parse(v.substring(29, 37), DATA);
            var valor = new Dinheiro(new BigDecimal(v.substring(37, 47)).movePointLeft(2));
            return new BoletoAPagar(id, linha, valor, vencimento, "Fornecedor " + v.substring(25, 29));
        } catch (DateTimeParseException e) {
            throw new BoletoIndisponivelNoEmissorException(linha); // erro do lado de lá vira exceção de domínio
        }
    }
}
