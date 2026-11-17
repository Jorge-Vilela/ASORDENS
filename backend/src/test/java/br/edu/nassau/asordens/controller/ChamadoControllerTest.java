package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.dto.ChamadoRequestDTO;
import br.edu.nassau.asordens.dto.ChamadoResponseDTO;
import br.edu.nassau.asordens.model.PrioridadeChamado;
import br.edu.nassau.asordens.service.ChamadoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChamadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChamadoService chamadoService;

    @Test
    @DisplayName("GET /api/chamados deve retornar 200 OK para usuário autenticado")
    void deveRetornar200AoListarChamados() throws Exception {
        when(chamadoService.listarFiltros(any(), any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/chamados")
                        .with(user("user@asordens.com").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/chamados deve retornar 201 Created com payload válido")
    void deveRetornar201AoCriarChamado() throws Exception {
        var dto = new ChamadoRequestDTO("Impressora parada", "Sem toner", 1L, null, PrioridadeChamado.MEDIA, null);
        var response = org.mockito.Mockito.mock(ChamadoResponseDTO.class);

        when(chamadoService.criarChamado(any())).thenReturn(response);

        mockMvc.perform(post("/api/chamados")
                        .with(user("user@asordens.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/chamados deve retornar 400 Bad Request quando título estiver em branco")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        var dtoInvalido = new ChamadoRequestDTO("", "", null, null, null, null);

        mockMvc.perform(post("/api/chamados")
                        .with(user("user@asordens.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/chamados/{id} deve retornar 403 Forbidden para perfil USER")
    void deveRetornar403QuandoUserTentarDeletarChamado() throws Exception {
        mockMvc.perform(delete("/api/chamados/1")
                        .with(user("user@asordens.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/chamados/{id} deve retornar 404 Not Found quando ID não existir")
    void deveRetornar404QuandoChamadoNaoExistir() throws Exception {
        when(chamadoService.buscar(999L)).thenThrow(new EntityNotFoundException("Chamado não encontrado."));

        mockMvc.perform(get("/api/chamados/999")
                        .with(user("admin@asordens.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}