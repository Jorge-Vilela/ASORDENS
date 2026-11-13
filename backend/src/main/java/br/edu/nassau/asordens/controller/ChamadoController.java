package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.chamado.Chamado;
import br.edu.nassau.asordens.service.ChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService service;

    @PostMapping
    public ResponseEntity<Chamado> criar(@RequestBody Chamado chamado) {
        Chamado novoChamado = service.criarChamado(chamado);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoChamado);
    }
    @GetMapping
    public ResponseEntity<List<Chamado>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Chamado> atualizar(@PathVariable Long id, @RequestBody Chamado chamado) {
        try {
            Chamado chamadoAtualizado = service.atualizarChamado(id, chamado);
            return ResponseEntity.ok(chamadoAtualizado);
        }catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Chamado> atualizarStatus(@PathVariable Long id, @RequestBody Chamado chamado) {
        try {
            Chamado chamadoAtualizado = service.atualizarStatus(id, chamado.getStatus());
            return ResponseEntity.ok(chamadoAtualizado);
        }catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            service.deletarChamado(id);
            return ResponseEntity.noContent().build();
        }catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
