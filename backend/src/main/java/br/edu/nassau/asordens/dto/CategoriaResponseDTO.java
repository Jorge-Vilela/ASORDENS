package br.edu.nassau.asordens.dto;

import br.edu.nassau.asordens.model.Categoria;

public record CategoriaResponseDTO(
        Long id,
        String nome,
        Integer horas
) {
    public static CategoriaResponseDTO fromEntity(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getHoras()
        );
    }
}
