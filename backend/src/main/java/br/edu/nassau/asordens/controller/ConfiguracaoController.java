package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.configuracao.*;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional
public class ConfiguracaoController {
    private final ConfiguracaoService service;
    private final ChamadoRepository chamados;

    @GetMapping("/configuracoes")
    public Configuracao configuracoes() { return service.atual(); }

    @PutMapping("/me")
    public Configuracao perfil(@RequestBody Map<String, String> dados) {
        var config = service.atual();
        var disponibilidade = dados.get("disponibilidade");
        if (!List.of("online", "ocupado", "ausente").contains(disponibilidade == null ? "" : disponibilidade))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disponibilidade inválida.");
        config.setEmail(dados.getOrDefault("email", ""));
        config.setTelefone(dados.getOrDefault("telefone", ""));
        config.setDisponibilidade(disponibilidade);
        return service.salvar(config);
    }

    @PutMapping("/me/preferencias")
    public Configuracao preferencias(@RequestBody Map<String, Boolean> dados) {
        var config = service.atual();
        if (!config.getPreferencias().keySet().equals(dados.keySet()) || dados.containsValue(null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preferências inválidas.");
        config.setPreferencias(new HashMap<>(dados));
        return service.salvar(config);
    }

    @PostMapping("/categorias")
    public Configuracao adicionar(@RequestBody Map<String, String> dados) {
        var config = service.atual();
        var nome = dados.getOrDefault("nome", "").trim();
        if (nome.isBlank() || nome.length() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma categoria de até 100 caracteres.");
        if (config.getCategorias().stream().anyMatch(c -> c.equalsIgnoreCase(nome)))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa categoria já existe.");
        config.setCategorias(new ArrayList<>(config.getCategorias()));
        config.getCategorias().add(nome);
        return service.salvar(config);
    }

    @DeleteMapping("/categorias/{nome}")
    public Configuracao remover(@PathVariable String nome) {
        if (chamados.existsByCategoria(nome))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível remover uma categoria em uso.");
        var config = service.atual();
        var categorias = new ArrayList<>(config.getCategorias());
        if (!categorias.remove(nome)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada.");
        config.setCategorias(categorias);
        return service.salvar(config);
    }

    @GetMapping("/agentes")
    public List<Map<String, Object>> agentes() {
        var config = service.atual();
        // O projeto ainda não possui autenticação: um perfil local compartilhado.
        return List.of(Map.of("id", config.getId(), "nome", config.getNome(), "cargo", config.getCargo(),
            "equipe", config.getEquipe(), "email", config.getEmail(), "disponibilidade", config.getDisponibilidade()));
    }
}
