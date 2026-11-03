package com.example.boleto.adapter.notificacao;

import com.example.boleto.domain.entity.Boleto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notificação ao pagador, injetada DIRETAMENTE no use case (sem interface).
 *
 * <p>SIMULAÇÃO: na vida real seria um cliente de e-mail/SMS/fila. Aqui só escreve no log e guarda o
 * que "enviou" (útil para testes e para ver a simulação). Detalhes do sistema externo (DTOs, URLs,
 * erros técnicos) ficam nesta classe; quem chama só conhece o {@link Boleto}.
 */
@ApplicationScoped
public class NotificadorBoleto {

    private static final Logger LOG = Logger.getLogger(NotificadorBoleto.class);

    private final List<NotificacaoEnviada> enviadas = new CopyOnWriteArrayList<>();

    public void notificarEmissao(Boleto boleto) {
        var notificacao = new NotificacaoEnviada(boleto.getPagador().email(), boleto.getLinhaDigitavel().valor());
        enviadas.add(notificacao);
        LOG.infof("[SIMULAÇÃO] E-mail para %s: boleto de %s vence em %s. Linha digitável: %s",
                notificacao.destinatario(), boleto.getValor(), boleto.getDataVencimento(), notificacao.linhaDigitavel());
    }

    public List<NotificacaoEnviada> enviadas() {
        return List.copyOf(enviadas);
    }

    public record NotificacaoEnviada(String destinatario, String linhaDigitavel) {
    }
}
