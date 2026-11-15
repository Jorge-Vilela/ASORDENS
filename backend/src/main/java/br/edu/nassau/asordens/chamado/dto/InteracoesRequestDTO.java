package br.edu.nassau.asordens.chamado.dto;

import jakarta.validation.constraints.NotBlank;

public record InteracoesRequestDTO(
        @NotBlank(message = "O texto da mensagem é obrigatório. ")
        String texto
){}
