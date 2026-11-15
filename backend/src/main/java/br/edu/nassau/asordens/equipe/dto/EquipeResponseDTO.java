package br.edu.nassau.asordens.equipe.dto;

import br.edu.nassau.asordens.equipe.Equipe;

public record EquipeResponseDTO(

        Long id,
        String nome,
        String descricao,
        Boolean ativo
){
    public static EquipeResponseDTO fromEntity(Equipe equipe) {
        return new EquipeResponseDTO(
                equipe.getId(),
                equipe.getNome(),
                equipe.getDescricao(),
                equipe.getAtivo()
        );
    }
}
