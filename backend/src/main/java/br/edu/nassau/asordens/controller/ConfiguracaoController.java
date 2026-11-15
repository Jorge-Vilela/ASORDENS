package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.configuracao.ConfiguracaoService;
import br.edu.nassau.asordens.configuracao.dto.ConfiguracaoResponseDTO;
import br.edu.nassau.asordens.configuracao.dto.PerfilConfigRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConfiguracaoController {

    private final ConfiguracaoService service;

    @GetMapping("/configuracoes")
    public ResponseEntity<ConfiguracaoResponseDTO> configuracoes() {
        return ResponseEntity.ok(ConfiguracaoResponseDTO.fromEntity(service.atual()));
    }

    @PutMapping("/me")
    public ResponseEntity<ConfiguracaoResponseDTO> perfil(@Valid @RequestBody PerfilConfigRequestDTO dados) {
        var config = service.atual();
        config.setEmail(dados.email() != null ? dados.email() : "");
        config.setTelefone(dados.telefone() != null ? dados.telefone() : "");
        config.setDisponibilidade(dados.disponibilidade());
        return ResponseEntity.ok(ConfiguracaoResponseDTO.fromEntity(service.salvar(config)));
    }

    @PutMapping("/me/preferencias")
    public ResponseEntity<ConfiguracaoResponseDTO> preferencias(@RequestBody Map<String, Boolean> dados) {
        var config = service.atual();
        if (!config.getPreferencias().keySet().equals(dados.keySet()) || dados.containsValue(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preferências inválidas.");
        }
        config.setPreferencias(new HashMap<>(dados));
        return ResponseEntity.ok(ConfiguracaoResponseDTO.fromEntity(service.salvar(config)));
    }

    @GetMapping("/agentes")
    public ResponseEntity<List> agentes() {
        var config = service.atual();
        return ResponseEntity.ok(List.of(Map.of(
                "id", config.getId(),
                "nome", config.getNome(),
                "cargo", config.getCargo(),
                "equipe", config.getEquipe(),
                "email", config.getEmail(),
                "disponibilidade", config.getDisponibilidade()
        )));
    }
}