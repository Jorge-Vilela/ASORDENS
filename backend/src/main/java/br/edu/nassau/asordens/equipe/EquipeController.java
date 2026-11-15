package br.edu.nassau.asordens.equipe;

import br.edu.nassau.asordens.equipe.dto.EquipeRequestDTO;
import br.edu.nassau.asordens.equipe.dto.EquipeResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

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
