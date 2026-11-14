package br.edu.nassau.asordens;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:integracao;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IntegracaoTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json")
            .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
            .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void cicloCompletoPersisteCamposMensagensAnexosEConfiguracoes() throws Exception {
        var dados = """
            {"titulo":"Impressora","solicitante":"Maria","categoria":"Hardware","prioridade":"alta",
             "descricao":"Sem conexão","anexos":[{"nome":"teste.txt","tipo":"text/plain","url":"data:text/plain;base64,b2xh"}]}
            """;
        var criado = request("POST", "/chamados", dados);
        assertEquals(201, criado.statusCode(), criado.body());
        var matcher = Pattern.compile("\"id\":(\\d+)").matcher(criado.body());
        assertTrue(matcher.find());
        var path = "/chamados/" + matcher.group(1);
        assertTrue(criado.body().contains("\"status\":\"aberto\""));
        assertEquals(200, request("PUT", path, dados.replace("Impressora", "Impressora corrigida")).statusCode());
        assertEquals(200, request("POST", path + "/puxar", null).statusCode());
        assertEquals(409, request("POST", path + "/puxar", null).statusCode());
        assertEquals(200, request("POST", path + "/mensagens", "{\"texto\":\"Verificando\",\"anexos\":[]}").statusCode());
        assertEquals(400, request("PATCH", path + "/status", "{\"status\":\"invalido\"}").statusCode());
        assertEquals(200, request("PATCH", path + "/status", "{\"status\":\"resolvido\"}").statusCode());
        assertEquals(200, request("PUT", path, dados.replace("Impressora", "Impressora corrigida")).statusCode());
        var salvo = request("GET", path, null);
        assertEquals(200, salvo.statusCode());
        for (var texto : new String[]{"Maria", "Hardware", "alta", "Impressora corrigida", "Verificando", "data:text/plain;base64,b2xh", "Andre Moura"})
            assertTrue(salvo.body().contains(texto), salvo.body());
        assertFalse(salvo.body().contains("\"resolvidoEm\":null"));
        assertEquals(409, request("DELETE", "/categorias/Hardware", null).statusCode());
        assertEquals(400, request("POST", "/chamados", "{}").statusCode());
        assertEquals(404, request("GET", "/chamados/999999", null).statusCode());
        assertEquals(200, request("PUT", "/me", "{\"email\":\"teste@example.com\",\"telefone\":\"123\",\"disponibilidade\":\"ocupado\"}").statusCode());
        assertEquals(200, request("PUT", "/me/preferencias", "{\"notificarNovoChamado\":false,\"notificarRespostaSolicitante\":true,\"notificarPorEmail\":false}").statusCode());
        assertTrue(request("GET", "/configuracoes", null).body().contains("teste@example.com"));
        assertTrue(request("GET", "/agentes", null).body().contains("ocupado"));
        assertEquals(200, request("POST", "/categorias", "{\"nome\":\"Teste\"}").statusCode());
        assertEquals(409, request("POST", "/categorias", "{\"nome\":\"teste\"}").statusCode());
        assertEquals(200, request("DELETE", "/categorias/Teste", null).statusCode());
        assertEquals(204, request("DELETE", path, null).statusCode());
        assertEquals(404, request("GET", path, null).statusCode());
    }

    @Test
    void somenteUmAtendentePodePuxarOChamado() throws Exception {
        var criado = request("POST", "/chamados", "{\"titulo\":\"Concorrência\",\"solicitante\":\"Teste\",\"categoria\":\"Hardware\",\"prioridade\":\"baixa\"}");
        assertEquals(201, criado.statusCode(), criado.body());
        var matcher = Pattern.compile("\"id\":(\\d+)").matcher(criado.body());
        assertTrue(matcher.find());
        var path = "/chamados/" + matcher.group(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var inicio = new CountDownLatch(1);
            Callable<Integer> puxar = () -> { inicio.await(); return request("POST", path + "/puxar", null).statusCode(); };
            var primeira = executor.submit(puxar);
            var segunda = executor.submit(puxar);
            inicio.countDown();
            assertEquals(java.util.Set.of(200, 409), java.util.Set.of(primeira.get(10, TimeUnit.SECONDS), segunda.get(10, TimeUnit.SECONDS)));
        } finally {
            executor.shutdownNow();
            request("DELETE", path, null);
        }
    }
}
