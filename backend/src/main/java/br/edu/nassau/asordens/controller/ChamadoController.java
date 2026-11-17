package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.dto.*;
import br.edu.nassau.asordens.model.PrioridadeChamado;
import br.edu.nassau.asordens.model.StatusChamado;
import br.edu.nassau.asordens.service.ChamadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService service;

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criar(@Valid @RequestBody ChamadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarChamado(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ChamadoResponseDTO>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) PrioridadeChamado prioridade,
            @RequestParam(required = false) Long equipeId,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long agenteId,
            @RequestParam(defaultValue = "false") boolean semAgente,
            @RequestParam(defaultValue = "false") boolean somenteAtivos,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listarFiltros(status, prioridade, equipeId, categoriaId, agenteId, semAgente, somenteAtivos, titulo, pageable));
    }

    @GetMapping("/busca")
    public ResponseEntity<Page<ChamadoResponseDTO>> buscarPorTitulo(@RequestParam String titulo, Pageable pageable) {
        return ResponseEntity.ok(service.buscarPorTitulo(titulo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> atualizar(
            @PathVariable Long id, @Valid @RequestBody ChamadoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarChamado(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ChamadoResponseDTO> status(
            @PathVariable Long id, @RequestBody Map<String, String> dados) {
        return ResponseEntity.ok(service.atualizarStatus(id, StatusChamado.fromString(dados.get("status"))));
    }
    @PatchMapping("/{id}/assumir")
    public ResponseEntity<ChamadoResponseDTO> assumir(@PathVariable Long id) {
        return ResponseEntity.ok(service.puxar(id));
    }
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ChamadoResponseDTO> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(service.finalizar(id));
    }

    @PostMapping("/{id}/puxar")
    public ResponseEntity<ChamadoResponseDTO> puxar(@PathVariable Long id) {
        return ResponseEntity.ok(service.puxar(id));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<ChamadoResponseDTO> mensagem(
            @PathVariable Long id, @Valid @RequestBody InteracoesRequestDTO dto) {
        return ResponseEntity.ok(service.enviarMensagem(id, dto));
    }

    @PostMapping("/{id}/avaliacao")
    public ResponseEntity<ChamadoResponseDTO> avaliar(
            @PathVariable Long id, @Valid @RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(service.avaliarChamado(id, dto));
    }
    @PostMapping("/{id}/interacoes")
    public ResponseEntity<ChamadoResponseDTO> criarInteracao(
            @PathVariable Long id, @Valid @RequestBody InteracoesRequestDTO dto) {
        return ResponseEntity.ok(service.enviarMensagem(id, dto));
    }
    @PostMapping("/{id}/anexos")
    public ResponseEntity<ChamadoResponseDTO> adicionarAnexo(
            @PathVariable Long id, @Valid @RequestBody AnexoDTO dto) {
        return ResponseEntity.ok(service.adicionarAnexo(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        service.deletarChamado(id);
    }
}
