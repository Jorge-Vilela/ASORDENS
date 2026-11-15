package br.edu.nassau.asordens.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PerfilConfigRequestDTO(
        @Email(message = "E-mail inválido")
        String email,

        String telefone,

        @NotBlank(message = "A disponibilidade é obrigatória")
        @Pattern(regexp = "online|ocupado|ausente", message = "Disponibilidade deve ser: online, ocupado ou ausente")
        String disponibilidade
) {}