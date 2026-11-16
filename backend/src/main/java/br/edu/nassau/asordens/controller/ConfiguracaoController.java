package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.model.Perfil;
import br.edu.nassau.asordens.repository.UsuarioRepository;
import br.edu.nassau.asordens.service.ConfiguracaoService;
import br.edu.nassau.asordens.dto.ConfiguracaoResponseDTO;
import br.edu.nassau.asordens.dto.PerfilConfigRequestDTO;
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
    private final UsuarioRepository usuarioRepository;

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
    public ResponseEntity agentes() {
        var lista = usuarioRepository.findAll().stream()
                .filter(u -> u.getAtivo() && (u.getPerfil() == Perfil.AGENTE || u.getPerfil() == Perfil.ADMIN))
                .map(u -> Map.of(
                        "id", u.getId(),
                        "nome", u.getNome(),
                        "email", u.getEmail(),
                        "perfil", u.getPerfil().name(),
                        "equipe", u.getEquipe() != null ? u.getEquipe().getNome() : "Sem equipe"
                ))
                .toList();
        return ResponseEntity.ok(lista);
    }
}