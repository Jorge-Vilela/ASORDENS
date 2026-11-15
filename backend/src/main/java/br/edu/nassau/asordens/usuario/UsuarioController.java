package br.edu.nassau.asordens.usuario;

import br.edu.nassau.asordens.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarPaginado(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/perfil")
    public ResponseEntity<UsuarioResponseDTO> alterarPerfil(
            @PathVariable Long id, @RequestParam Perfil perfil) {
        return ResponseEntity.ok(usuarioService.alterarPerfil(id, perfil));
    }

    @PatchMapping("/{id}/equipe")
    public ResponseEntity<UsuarioResponseDTO> vincularEquipe(
            @PathVariable Long id, @RequestParam(required = false) Long equipeId) {
        return ResponseEntity.ok(usuarioService.vincularEquipe(id, equipeId));
    }
}
