package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.dto.AvaliacaoRequestDTO;
import br.edu.nassau.asordens.dto.ChamadoRequestDTO;
import br.edu.nassau.asordens.model.*;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import br.edu.nassau.asordens.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private EquipeService equipeService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    private Usuario usuarioMock;
    private Categoria categoriaMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNome("Jorge Vilela");
        usuarioMock.setEmail("jorge@asordens.com");
        usuarioMock.setPerfil(Perfil.AGENTE);

        categoriaMock = new Categoria();
        categoriaMock.setId(1L);
        categoriaMock.setNome("Software");

        var auth = new UsernamePasswordAuthenticationToken("jorge@asordens.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve criar um chamado com sucesso e definir status ABERTO")
    void criarChamadoComSucesso() {
        var dto = new ChamadoRequestDTO("Erro no sistema", "Tela azul ao salvar", 1L, null, PrioridadeChamado.ALTA, null);

        when(usuarioRepository.findByEmail("jorge@asordens.com")).thenReturn(Optional.of(usuarioMock));
        when(categoriaService.buscarEntidade(1L)).thenReturn(categoriaMock);
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> {
            Chamado c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        var response = chamadoService.criarChamado(dto);

        assertNotNull(response);
        assertEquals("Erro no sistema", response.titulo());
        assertEquals("aberto", response.status());
        verify(chamadoRepository, times(1)).save(any(Chamado.class));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar chamado inexistente")
    void lancarExcecaoAoBuscarChamadoInexistente() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> chamadoService.buscar(99L));
        verify(chamadoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve assumir chamado aberto e registrar interação automática")
    void assumirChamadoComSucesso() {
        var chamado = new Chamado();
        chamado.setId(5L);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setPrioridade(PrioridadeChamado.MEDIA);
        chamado.setSolicitante(usuarioMock);
        chamado.setCategoria(categoriaMock);
        chamado.setInteracoes(new ArrayList<>());

        when(chamadoRepository.buscarParaAtualizar(5L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findByEmail("jorge@asordens.com")).thenReturn(Optional.of(usuarioMock));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = chamadoService.puxar(5L);

        assertEquals("em_andamento", response.status());
        assertEquals(1, chamado.getInteracoes().size());
        verify(chamadoRepository, times(1)).save(chamado);
    }

    @Test
    @DisplayName("Deve bloquear avaliação quando chamado ainda estiver ABERTO")
    void bloquearAvaliacaoDeChamadoAberto() {
        var chamado = new Chamado();
        chamado.setId(7L);
        chamado.setStatus(StatusChamado.ABERTO);

        when(chamadoRepository.buscarParaAtualizar(7L)).thenReturn(Optional.of(chamado));

        var avaliacaoDto = new AvaliacaoRequestDTO(5, "Ótimo atendimento");

        assertThrows(ResponseStatusException.class, () -> chamadoService.avaliarChamado(7L, avaliacaoDto));
        verify(chamadoRepository, never()).save(any());
    }
}