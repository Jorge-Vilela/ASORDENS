package br.edu.nassau.asordens;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integracao;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "api.security.token.secret=chave-exclusiva-para-testes-automatizados"
})
class IntegracaoTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private br.edu.nassau.asordens.repository.UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;

    @Test
    void administradorExcluiUsuarioSemHistoricoEConfiguracao() throws Exception {
        String email = "excluir_" + UUID.randomUUID().toString().substring(0, 6) + "@teste.com";
        String token = registrarELogar("Usuario Exclusao", email, "123456", "USER");
        Long id = usuarioRepository.findByEmail(email).orElseThrow().getId();
        mockMvc.perform(get("/api/configuracoes").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/usuarios/" + id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
        assertTrue(usuarioRepository.findByEmail(email).isEmpty());
        mockMvc.perform(get("/api/chamados").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComChamadoNaoPodeSerExcluido() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 6);
        String email = "vinculado_" + sufixo + "@teste.com";
        String token = registrarELogar("Usuario Vinculado", email, "123456", "USER");
        String tokenAdmin = registrarELogar("Admin Vinculado", "admin_v_" + sufixo + "@teste.com", "123456", "ADMIN");
        Long categoriaId = criarCategoria(tokenAdmin, "Categoria_" + sufixo, 4);
        mockMvc.perform(post("/api/chamados").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Teste\",\"categoriaId\":" + categoriaId + ",\"prioridade\":\"BAIXA\"}"))
                .andExpect(status().isCreated());
        Long id = usuarioRepository.findByEmail(email).orElseThrow().getId();
        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isConflict());
        assertTrue(usuarioRepository.findByEmail(email).isPresent());
    }

    @Test
    void usuarioDesativadoNaoPodeUsarTokenEmitidoAntesDaDesativacao() throws Exception {
        String email = "desativado_" + UUID.randomUUID().toString().substring(0, 6) + "@teste.com";
        String token = registrarELogar("Usuario Teste", email, "123456", "USER");
        MvcResult usuarios = mockMvc.perform(get("/api/usuarios").param("size", "1000")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andReturn();
        String body = usuarios.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\\{[^{}]*\\\"id\\\":(\\d+)[^{}]*\\\"email\\\":\\\"" + Pattern.quote(email) + "\\\"").matcher(body);
        assertTrue(matcher.find());
        mockMvc.perform(patch("/api/usuarios/" + matcher.group(1) + "/desativar")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/chamados").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void primeiroAcessoCriaConfiguracaoDoUsuario() throws Exception {
        String email = "perfil_" + UUID.randomUUID().toString().substring(0, 6) + "@teste.com";
        String token = registrarELogar("Usuario Perfil", email, "123456", "AGENTE");
        mockMvc.perform(get("/api/configuracoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        mockMvc.perform(get("/api/configuracoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void cicloCompletoPersisteCamposMensagensAnexosEAvaliacao() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 6);
        String tokenAdmin = registrarELogar("Admin Teste", "admin_" + sufixo + "@teste.com", "123456", "ADMIN");
        String tokenUser = registrarELogar("Solicitante Teste", "user_" + sufixo + "@teste.com", "123456", "USER");

        Long categoriaId = criarCategoria(tokenAdmin, "Rede_" + sufixo, 4);

        String payloadChamado = """
            {
              "titulo": "Queda de conexão no setor financeiro",
              "descricao": "Sem acesso à rede interna desde as 09h.",
              "categoriaId": %d,
              "prioridade": "alta",
              "anexos": [
                {
                  "nome": "log-rede.txt",
                  "tipo": "text/plain",
                  "tamanho": 16,
                  "url": "data:text/plain;base64,T2zDoSBtdW5kbw=="
                }
              ]
            }
            """.formatted(categoriaId);

        MvcResult criacao = mockMvc.perform(post("/api/chamados")
                        .header("Authorization", "Bearer " + tokenUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadChamado))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("aberto"))
                .andExpect(jsonPath("$.anexos[0].nome").value("log-rede.txt"))
                .andReturn();

        Long chamadoId = extrairId(criacao.getResponse().getContentAsString());

        mockMvc.perform(post("/api/chamados/" + chamadoId + "/puxar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("em_andamento"));

        mockMvc.perform(post("/api/chamados/" + chamadoId + "/mensagens")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "texto": "Switch reiniciado, favor validar conexão." }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagens").isNotEmpty());

        mockMvc.perform(patch("/api/chamados/" + chamadoId + "/status")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "status": "resolvido" }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("resolvido"));

        mockMvc.perform(post("/api/chamados/" + chamadoId + "/avaliacao")
                        .header("Authorization", "Bearer " + tokenUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "nota": 5, "comentario": "Atendimento rápido e eficiente." }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avaliacao.nota").value(5));
    }

    @Test
    void somenteUmAtendentePodePuxarOChamado() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 6);
        String tokenAdmin = registrarELogar("Admin Concorrencia", "adm_conc_" + sufixo + "@teste.com", "123456", "ADMIN");
        String tokenAgente1 = registrarELogar("Agente 1", "ag1_" + sufixo + "@teste.com", "123456", "AGENTE");
        String tokenAgente2 = registrarELogar("Agente 2", "ag2_" + sufixo + "@teste.com", "123456", "AGENTE");

        Long categoriaId = criarCategoria(tokenAdmin, "Sistemas_" + sufixo, 8);

        MvcResult criacao = mockMvc.perform(post("/api/chamados")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "titulo": "Erro ao emitir nota fiscal",
                      "descricao": "Timeout na API da SEFAZ",
                      "categoriaId": %d,
                      "prioridade": "media"
                    }
                    """.formatted(categoriaId)))
                .andExpect(status().isCreated())
                .andReturn();

        Long chamadoId = extrairId(criacao.getResponse().getContentAsString());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicioSimultaneo = new CountDownLatch(1);

        Callable<Integer> tentativaAgente1 = () -> {
            inicioSimultaneo.await();
            return mockMvc.perform(post("/api/chamados/" + chamadoId + "/puxar")
                            .header("Authorization", "Bearer " + tokenAgente1))
                    .andReturn().getResponse().getStatus();
        };

        Callable<Integer> tentativaAgente2 = () -> {
            inicioSimultaneo.await();
            return mockMvc.perform(post("/api/chamados/" + chamadoId + "/puxar")
                            .header("Authorization", "Bearer " + tokenAgente2))
                    .andReturn().getResponse().getStatus();
        };

        Future<Integer> res1 = executor.submit(tentativaAgente1);
        Future<Integer> res2 = executor.submit(tentativaAgente2);

        inicioSimultaneo.countDown();
        List<Integer> statusCodes = List.of(res1.get(), res2.get());
        executor.shutdown();

        assertTrue(statusCodes.contains(200));
        assertTrue(statusCodes.contains(409));
        assertEquals(2, statusCodes.size());
    }

    private String registrarELogar(String nome, String email, String senha, String perfil) throws Exception {
        String registerJson = """
            {
              "nome": "%s",
              "email": "%s",
              "senha": "%s",
              "perfil": "%s"
            }
            """.formatted(nome, email, senha, perfil);

        mockMvc.perform(post("/api/usuarios")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email": "%s", "senha": "%s" }
                    """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("Token JWT não encontrado na resposta: " + body);
        }
        return matcher.group(1);
    }

    private Long criarCategoria(String tokenAdmin, String nome, int horas) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "nome": "%s", "horas": %d }
                    """.formatted(nome, horas)))
                .andExpect(status().isCreated())
                .andReturn();

        return extrairId(result.getResponse().getContentAsString());
    }

    private Long extrairId(String json) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("ID não encontrado na resposta: " + json);
        }
        return Long.parseLong(matcher.group(1));
    }
}
