package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.dto.ChamadoResponseDTO;
import br.edu.nassau.asordens.model.StatusChamado;
import br.edu.nassau.asordens.service.RelatorioService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {
    private final RelatorioService service;

    @GetMapping("/chamados")
    public ResponseEntity<List<ChamadoResponseDTO>> chamados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Long equipeId) {
        return ResponseEntity.ok(service.chamados(inicio, fim, status, equipeId));
    }

    @GetMapping("/por-categoria")
    public ResponseEntity<List<Map<String, Object>>> porCategoria() {
        return ResponseEntity.ok(service.porCategoria());
    }
}
