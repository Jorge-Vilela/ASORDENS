package br.edu.nassau.asordens.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank(message = "O nome da categoria é obrigatório! ")
        @Size(min = 2, max = 80, message = "O nome deve ter entre 2 e 80 caracteres! ")
        String nome,

        @NotNull(message = "As horas são obrigatórias! ")
        @Positive(message = "As horas devem ser maiores que zero! ")
        Integer horas
){}
