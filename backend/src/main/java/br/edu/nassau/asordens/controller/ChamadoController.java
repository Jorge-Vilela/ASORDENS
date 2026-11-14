package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.chamado.Chamado;
import br.edu.nassau.asordens.service.ChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {
    private final ChamadoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Chamado criar(@RequestBody Chamado chamado) { return service.criarChamado(chamado); }

    @GetMapping
    public List<Chamado> listar() { return service.listarTodos(); }

    @GetMapping("/{id}")
    public Chamado buscar(@PathVariable Long id) { return service.buscar(id); }

    @PutMapping("/{id}")
    public Chamado atualizar(@PathVariable Long id, @RequestBody Chamado chamado) { return service.atualizarChamado(id, chamado); }

    @PatchMapping("/{id}/status")
    public Chamado status(@PathVariable Long id, @RequestBody Map<String, String> dados) { return service.atualizarStatus(id, dados.get("status")); }

    @PostMapping("/{id}/puxar")
    public Chamado puxar(@PathVariable Long id) { return service.puxar(id); }

    @PostMapping("/{id}/mensagens")
    public Chamado mensagem(@PathVariable Long id, @RequestBody Chamado.Mensagem dados) { return service.enviarMensagem(id, dados); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) { service.deletarChamado(id); }
}
