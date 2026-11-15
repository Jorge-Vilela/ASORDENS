package br.edu.nassau.asordens.dto;

import br.edu.nassau.asordens.model.Perfil;
import br.edu.nassau.asordens.model.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        Boolean ativo,
        Long equipeId,
        String equipeNome
){
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAtivo(),
                usuario.getEquipe() != null ? usuario.getEquipe().getId() : null,
                usuario.getEquipe() != null ? usuario.getEquipe().getNome() : null
        );
    }
}
