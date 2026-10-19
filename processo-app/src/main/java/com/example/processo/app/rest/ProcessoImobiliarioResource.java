package com.example.processo.app.rest;

import com.example.processo.core.motor.application.MotorDeProcessamento;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.Solicitacao;
import com.example.processo.core.segmento.imobiliario.domain.DadosImobiliarios;
import com.example.processo.query.DetalheImobiliario;
import com.example.processo.query.ProcessoQueries;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Endpoints do segmento imobiliário. A criação entrega a solicitação ao motor único (processo-core); a consulta
 * vai direto ao lado de leitura (processo-query). Os dois lados só se encontram aqui.
 */
@Path("/processos/imobiliarios")
public class ProcessoImobiliarioResource {

    public record NovoProcessoImobiliario(Proponente proponente, BigDecimal valor, Imovel imovel) {
    }

    public record Imovel(String matricula, DadosImobiliarios.TipoImovel tipo, BigDecimal valorAvaliacao, String uf) {
    }

    private final MotorDeProcessamento motor;
    private final ProcessoQueries consultas;

    ProcessoImobiliarioResource(MotorDeProcessamento motor, ProcessoQueries consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoImobiliario novo) {
        var imovel = novo.imovel() == null ? new Imovel(null, null, null, null) : novo.imovel();
        var dados = new DadosImobiliarios(imovel.matricula(), imovel.tipo(), imovel.valorAvaliacao(), imovel.uf());
        return Criado.em("/processos/imobiliarios", motor.criar(new Solicitacao<>(novo.proponente(), novo.valor(), dados)));
    }

    @GET
    @Path("{id}")
    public DetalheImobiliario detalhe(@PathParam("id") UUID id) {
        return consultas.imobiliario(id).orElseThrow(NotFoundException::new);
    }
}
