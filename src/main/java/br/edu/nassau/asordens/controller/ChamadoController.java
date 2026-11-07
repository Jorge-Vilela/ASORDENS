package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.model.Model;
import br.edu.nassau.asordens.service.ChamadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService service;

    @PostMapping
    public ResponseEntity<Model> criar(@RequestBody Model model) {
        Model novoModel = service.criarChamado(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoModel);
    }
    @GetMapping
    public ResponseEntity<List<Model>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

}
