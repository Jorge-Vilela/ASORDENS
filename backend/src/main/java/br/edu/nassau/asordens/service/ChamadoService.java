package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.dto.*;
import br.edu.nassau.asordens.model.*;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import br.edu.nassau.asordens.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ChamadoService {

    private final ChamadoRepository repository;
    private final CategoriaService categoriaService;
    private final EquipeService equipeService;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        return repository.findByTituloContainingIgnoreCase(titulo, pageable).map(ChamadoResponseDTO::fromEntity);
    }

    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO dto) {
        var chamado = new Chamado();
        chamado.setSolicitante(getUsuarioLogado());
        copiarDados(chamado, dto);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(OffsetDateTime.now());
        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> listarPaginado(
            StatusChamado status, PrioridadeChamado prioridade, Long equipeId, String titulo, Pageable pageable) {
        return repository.buscarComFiltros(status, prioridade, equipeId, titulo == null ? "" : titulo, pageable)
                .map(ChamadoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> listarFiltros(StatusChamado status, PrioridadeChamado prioridade, Long equipeId, Long categoriaId, Long agenteId, boolean semAgente, boolean somenteAtivos, String titulo, Pageable pageable) {
        return repository.buscarFiltrosCompletos(status, prioridade, equipeId, categoriaId, agenteId, semAgente, somenteAtivos, titulo == null ? "" : titulo, pageable).map(ChamadoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> listarPorEquipe(Long equipeId, Pageable pageable) {
        return repository.findByEquipeId(equipeId, pageable).map(ChamadoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ChamadoResponseDTO buscar(Long id) {
        Chamado chamado = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado."));
        return ChamadoResponseDTO.fromEntity(chamado);
    }

    private Chamado bloquear(Long id) {
        return repository.buscarParaAtualizar(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado."));
    }

    public ChamadoResponseDTO atualizarChamado(Long id, ChamadoRequestDTO dto) {
        var chamado = bloquear(id);
        copiarDados(chamado, dto);
        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    private void copiarDados(Chamado chamado, ChamadoRequestDTO dto) {
        chamado.setTitulo(dto.titulo().trim());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCategoria(categoriaService.buscarEntidade(dto.categoriaId()));
        chamado.setEquipe(dto.equipeId() != null ? equipeService.buscarEntidade(dto.equipeId()) : null);

        if (dto.anexos() != null) {
            List<AnexoChamado> novosAnexos = validarEConverterAnexos(dto.anexos(), chamado);
            chamado.getAnexos().clear();
            chamado.getAnexos().addAll(novosAnexos);
        }
    }

    public ChamadoResponseDTO atualizarStatus(Long id, StatusChamado novoStatus) {
        if (novoStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido.");
        }
        var chamado = bloquear(id);
        if (novoStatus == StatusChamado.RESOLVIDO || novoStatus == StatusChamado.FECHADO) {
            if (chamado.getResolvidoEm() == null) chamado.setResolvidoEm(OffsetDateTime.now());
        } else {
            chamado.setResolvidoEm(null);
        }
        chamado.setStatus(novoStatus);
        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    public ChamadoResponseDTO puxar(Long id) {
        var chamado = bloquear(id);
        var agenteLogado = getUsuarioLogado();

        if (chamado.getAgente() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este chamado já foi atribuído a um agente.");
        }
        if (chamado.getStatus() != StatusChamado.ABERTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente chamados abertos podem ser puxados.");
        }
        chamado.setAgente(agenteLogado);
        chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        if (chamado.getEquipe() == null && agenteLogado.getEquipe() != null) {
            chamado.setEquipe(agenteLogado.getEquipe());
        }

        var interacao = InteracaoChamado.builder()
                .chamado(chamado)
                .autor(agenteLogado)
                .texto("Chamado assumido por " + agenteLogado.getNome())
                .enviadoEm(OffsetDateTime.now())
                .build();
        chamado.getInteracoes().add(interacao);

        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }
    public ChamadoResponseDTO finalizar(Long id) {
        var chamado = bloquear(id);
        var usuarioLogado = getUsuarioLogado();

        if (chamado.getStatus() == StatusChamado.RESOLVIDO || chamado.getStatus() == StatusChamado.FECHADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este chamado já está finalizado.");
        }

        chamado.setStatus(StatusChamado.RESOLVIDO);
        if (chamado.getResolvidoEm() == null) {
            chamado.setResolvidoEm(OffsetDateTime.now());
        }

        var interacao = InteracaoChamado.builder()
                .chamado(chamado)
                .autor(usuarioLogado)
                .texto("Chamado finalizado por " + usuarioLogado.getNome())
                .enviadoEm(OffsetDateTime.now())
                .build();
        chamado.getInteracoes().add(interacao);

        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    public ChamadoResponseDTO adicionarAnexo(Long id, AnexoDTO anexoDto) {
        var chamado = bloquear(id);
        List<AnexoChamado> validados = validarEConverterAnexos(List.of(anexoDto), chamado);
        chamado.getAnexos().addAll(validados);
        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    public ChamadoResponseDTO enviarMensagem(Long id, @Valid InteracoesRequestDTO dto) {
        var chamado = bloquear(id);
        var autor = getUsuarioLogado();

        var interacao = InteracaoChamado.builder()
                .chamado(chamado)
                .autor(autor)
                .texto(dto.texto().trim())
                .enviadoEm(OffsetDateTime.now())
                .build();

        chamado.getInteracoes().add(interacao);
        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    public ChamadoResponseDTO avaliarChamado(Long id, AvaliacaoRequestDTO dto) {
        var chamado = bloquear(id);
        if (chamado.getStatus() != StatusChamado.RESOLVIDO && chamado.getStatus() != StatusChamado.FECHADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apenas chamados resolvidos ou fechados podem ser avaliados.");
        }
        if (chamado.getAvaliacao() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este chamado já foi avaliado.");
        }

        var avaliacao = AvaliacaoChamado.builder()
                .chamado(chamado)
                .nota(dto.nota())
                .comentario(dto.comentario())
                .dataAvaliacao(OffsetDateTime.now())
                .build();

        chamado.setAvaliacao(avaliacao);
        return ChamadoResponseDTO.fromEntity(salvar(chamado));
    }

    private List<AnexoChamado> validarEConverterAnexos(List<AnexoDTO> anexos, Chamado chamado) {
        if (anexos.size() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo de 10 anexos por envio.");
        }
        List<AnexoChamado> lista = new ArrayList<>();
        for (var anexo : anexos) {
            if (anexo == null || anexo.url() == null || !anexo.url().startsWith("data:") || !anexo.url().contains(";base64,")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anexo inválido.");
            }
            try {
                var bytes = Base64.getDecoder().decode(anexo.url().substring(anexo.url().indexOf(";base64,") + 8));
                if (bytes.length > 10 * 1024 * 1024) throw new IllegalArgumentException();

                lista.add(AnexoChamado.builder()
                        .nome(anexo.nome())
                        .tipo(anexo.tipo() != null ? anexo.tipo() : "application/octet-stream")
                        .tamanho(bytes.length)
                        .url(anexo.url())
                        .chamado(chamado)
                        .build());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anexo inválido ou maior que 10 MB.");
            }
        }
        return lista;
    }

    private Chamado salvar(Chamado chamado) {
        chamado.setAtualizadoEm(OffsetDateTime.now());
        return repository.save(chamado);
    }

    public void deletarChamado(Long id) {
        repository.delete(bloquear(id));
    }
}
