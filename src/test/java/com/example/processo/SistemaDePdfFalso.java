package com.example.processo;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Faz o papel do sistema de geração de PDF nos testes: guarda cada pedido recebido em /documentos e responde 204,
 * ou 500 quando a matrícula do imóvel começa com {@link #FALHA}. O jar testado recebe o endereço pela
 * configuração devolvida por {@link #start()}.
 */
public class SistemaDePdfFalso implements QuarkusTestResourceLifecycleManager {

    public static final String FALHA = "999";
    public static final List<String> PEDIDOS = new CopyOnWriteArrayList<>();

    private HttpServer servidor;

    @Override
    public Map<String, String> start() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/documentos", troca -> {
            var corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            PEDIDOS.add(corpo);
            troca.sendResponseHeaders(corpo.contains("\"matricula\":\"" + FALHA) ? 500 : 204, -1);
            troca.close();
        });
        servidor.start();
        return Map.of("quarkus.rest-client.sistema-pdf.url", "http://localhost:" + servidor.getAddress().getPort());
    }

    @Override
    public void stop() {
        servidor.stop(0);
    }
}
