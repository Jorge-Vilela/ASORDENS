package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.chamado.Chamado;
import br.edu.nassau.asordens.service.ChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

}
