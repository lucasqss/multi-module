package com.example.processo;

import io.restassured.path.json.JsonPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/** Lê a listagem paginada inteira, página a página: os ITs compartilham o banco e não sabem em que página cai o seu processo. */
final class Listagem {

    private Listagem() {
    }

    /** Todos os itens de {@code GET /processos}, com o filtro de situação se não for nulo. */
    static List<Map<String, Object>> todos(String situacao) {
        var itens = new ArrayList<Map<String, Object>>();
        for (int pagina = 1; ; pagina++) {
            var requisicao = given().queryParam("pagina", pagina).queryParam("tamanho", 100);
            if (situacao != null) {
                requisicao.queryParam("situacao", situacao);
            }
            JsonPath resposta = requisicao.get("/processos").then().statusCode(200).extract().jsonPath();
            List<Map<String, Object>> daPagina = resposta.getList("itens");
            itens.addAll(daPagina);
            if (daPagina.isEmpty() || itens.size() >= resposta.getLong("total")) {
                return itens;
            }
        }
    }

    static Map<String, Object> porNumero(List<Map<String, Object>> itens, String numero) {
        return itens.stream().filter(i -> numero.equals(i.get("numero"))).findFirst()
                .orElseThrow(() -> new AssertionError("Processo " + numero + " não está na listagem"));
    }
}
