package br.edu.nassau.asordens.dto;

import jakarta.validation.constraints.NotBlank;

public record AnexoDTO(
        Long id,
        @NotBlank(message = "Nome do anexo é obrigatório.")
        String nome,
        String tipo,
        long tamanho,
        @NotBlank(message = "Conteúdo do anexo é obrigatório.")
        String url
){}
