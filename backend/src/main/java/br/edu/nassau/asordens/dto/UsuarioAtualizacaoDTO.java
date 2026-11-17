package br.edu.nassau.asordens.dto;
import br.edu.nassau.asordens.model.Perfil;
import jakarta.validation.constraints.*;
public record UsuarioAtualizacaoDTO(@NotBlank @Size(min=3,max=120) String nome, @NotBlank @Email @Size(max=150) String email, @Size(min=6) String senha, @NotNull Perfil perfil, Long equipeId, @NotNull Boolean ativo) {}
