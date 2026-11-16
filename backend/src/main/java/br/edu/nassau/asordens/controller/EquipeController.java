package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.dto.ChamadoResponseDTO;
import br.edu.nassau.asordens.dto.EquipeRequestDTO;
import br.edu.nassau.asordens.dto.EquipeResponseDTO;
import br.edu.nassau.asordens.service.ChamadoService;
import br.edu.nassau.asordens.service.EquipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;
    private final ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<EquipeResponseDTO> criar(@Valid @RequestBody EquipeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipeService.criar(dto));
    }
    @GetMapping
    public ResponseEntity<List<EquipeResponseDTO>> listar() {
        return ResponseEntity.ok(equipeService.listarTodas());
    }
    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.buscarPorId(id));
    }

    @GetMapping("/{id}/chamados")
    public ResponseEntity<Page<ChamadoResponseDTO>> listarChamadosDaEquipe(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        equipeService.buscarEntidade(id);
        return ResponseEntity.ok(chamadoService.listarPorEquipe(id, pageable));
    }
    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EquipeRequestDTO dto) {
        return ResponseEntity.ok(equipeService.atualizar(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        equipeService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}