package br.edu.nassau.asordens.dto;

public record TokenResponseDTO(
        String token,
        String tipo,
        String email,
        String perfil
){}
