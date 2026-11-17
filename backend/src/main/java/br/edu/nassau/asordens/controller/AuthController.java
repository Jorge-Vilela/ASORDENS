package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.dto.LoginRequestDTO;
import br.edu.nassau.asordens.dto.TokenResponseDTO;
import br.edu.nassau.asordens.seguranca.JwtService;
import br.edu.nassau.asordens.service.UsuarioService;
import br.edu.nassau.asordens.dto.UsuarioRequestDTO;
import br.edu.nassau.asordens.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(new UsuarioRequestDTO(dto.nome(), dto.email(), dto.senha(), br.edu.nassau.asordens.model.Perfil.USER, null)));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.gerarToken(userDetails);
        String perfil = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return ResponseEntity.ok(new TokenResponseDTO(token, "Bearer", userDetails.getUsername(), perfil));
    }
}
