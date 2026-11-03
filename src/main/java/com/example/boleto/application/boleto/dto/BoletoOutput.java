package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.domain.valueobject.Email;
import com.example.boleto.domain.valueobject.LinhaDigitavel;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Saída dos casos de uso. A entidade de domínio nunca vaza para fora da application. */
public record BoletoOutput(
        UUID id,
        String nomePagador,
        String documentoPagador,
        String emailPagador,
        LocalDate dataVencimento,
        List<Item> itens,
        BigDecimal valor,
        List<String> destinatarios,
        String linhaDigitavel,
        StatusBoleto status,
        Instant criadoEm,
        Instant emitidoEm,
        Instant notificadoEm,
        BigDecimal valorPago,
        LocalDate dataPagamento,
        String motivoCancelamento) {

    public record Item(String descricao, BigDecimal valor) {
    }

    public static BoletoOutput de(Boleto b) {
        return new BoletoOutput(
                b.getId().valor(),
                b.getPagador().nome(),
                b.getPagador().documento().numero(),
                b.getPagador().email().valor(),
                b.getDataVencimento(),
                b.getItens().itens().stream().map(i -> new Item(i.descricao(), i.valor().valor())).toList(),
                b.getValor().valor(),
                b.getDestinatarios().emails().stream().map(Email::valor).toList(),
                b.getLinhaDigitavel().map(LinhaDigitavel::valor).orElse(null),
                b.getStatus(),
                b.getCriadoEm(),
                b.getEmitidoEm().orElse(null),
                b.getNotificadoEm().orElse(null),
                b.getPagamento().map(p -> p.valorPago().valor()).orElse(null),
                b.getPagamento().map(Pagamento::data).orElse(null),
                b.getMotivoCancelamento().map(Motivo::texto).orElse(null));
    }
}
