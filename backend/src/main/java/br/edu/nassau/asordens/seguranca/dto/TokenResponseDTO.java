package br.edu.nassau.asordens.seguranca.dto;

public record TokenResponseDTO(
        String token,
        String tipo,
        String email,
        String perfil
){}
