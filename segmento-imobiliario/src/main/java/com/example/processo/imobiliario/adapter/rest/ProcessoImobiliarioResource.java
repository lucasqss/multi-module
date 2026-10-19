package com.example.processo.imobiliario.adapter.rest;

import com.example.processo.imobiliario.domain.DadosImobiliarios;
import com.example.processo.motor.adapter.rest.Criado;
import com.example.processo.motor.application.MotorDeProcessamento;
import com.example.processo.motor.domain.Proponente;
import com.example.processo.motor.domain.Solicitacao;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

/** Cada segmento expõe o próprio endpoint (o corpo é dele); todos entregam a solicitação ao mesmo motor. */
@Path("/processos/imobiliarios")
public class ProcessoImobiliarioResource {

    public record NovoProcessoImobiliario(Proponente proponente, BigDecimal valor, Imovel imovel) {
    }

    public record Imovel(String matricula, DadosImobiliarios.TipoImovel tipo, BigDecimal valorAvaliacao, String uf) {
    }

    private final MotorDeProcessamento motor;

    ProcessoImobiliarioResource(MotorDeProcessamento motor) {
        this.motor = motor;
    }

    @POST
    public Response criar(NovoProcessoImobiliario novo) {
        var imovel = novo.imovel() == null ? new Imovel(null, null, null, null) : novo.imovel();
        var dados = new DadosImobiliarios(imovel.matricula(), imovel.tipo(), imovel.valorAvaliacao(), imovel.uf());
        return Criado.de(motor.criar(new Solicitacao<>(novo.proponente(), novo.valor(), dados)));
    }
}
