package com.example.processo.app.rest;

import com.example.processo.core.motor.application.MotorDeProcessamento;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.Solicitacao;
import com.example.processo.core.segmento.veicular.domain.DadosVeiculares;
import com.example.processo.query.DetalheVeicular;
import com.example.processo.query.ProcessoQueries;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.UUID;

/** Endpoints do segmento veicular: mesmo formato do imobiliário, mesmo motor. */
@Path("/processos/veiculares")
public class ProcessoVeicularResource {

    public record NovoProcessoVeicular(Proponente proponente, BigDecimal valor, Veiculo veiculo) {
    }

    public record Veiculo(String placa, Integer anoFabricacao, BigDecimal valorFipe) {
    }

    private final MotorDeProcessamento motor;
    private final ProcessoQueries consultas;

    ProcessoVeicularResource(MotorDeProcessamento motor, ProcessoQueries consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoVeicular novo) {
        var veiculo = novo.veiculo() == null ? new Veiculo(null, null, null) : novo.veiculo();
        var dados = new DadosVeiculares(veiculo.placa(), veiculo.anoFabricacao(), veiculo.valorFipe());
        return Criado.em("/processos/veiculares", motor.criar(new Solicitacao<>(novo.proponente(), novo.valor(), dados)));
    }

    @GET
    @Path("{id}")
    public DetalheVeicular detalhe(@PathParam("id") UUID id) {
        return consultas.veicular(id).orElseThrow(NotFoundException::new);
    }
}
