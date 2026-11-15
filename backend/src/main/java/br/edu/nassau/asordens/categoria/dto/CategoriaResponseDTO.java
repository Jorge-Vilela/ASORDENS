package br.edu.nassau.asordens.categoria.dto;

import br.edu.nassau.asordens.categoria.Categoria;

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
