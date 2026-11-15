package br.edu.nassau.asordens.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipeRequestDTO(
        @NotBlank(message = "Nome da equipe é obrigatório! ")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres! ")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres! ")
        String descricao,

        Boolean ativo
){}
