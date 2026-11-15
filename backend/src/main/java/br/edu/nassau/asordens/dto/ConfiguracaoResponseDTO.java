package br.edu.nassau.asordens.dto;

import br.edu.nassau.asordens.model.Configuracao;
import java.util.Map;

public record ConfiguracaoResponseDTO(
        Long id,
        String nome,
        String cargo,
        String equipe,
        String email,
        String telefone,
        String disponibilidade,
        Map<String, Boolean> preferencias
) {
    public static ConfiguracaoResponseDTO fromEntity(Configuracao c) {
        return new ConfiguracaoResponseDTO(
                c.getId(),
                c.getNome(),
                c.getCargo(),
                c.getEquipe(),
                c.getEmail(),
                c.getTelefone(),
                c.getDisponibilidade(),
                c.getPreferencias()
        );
    }
}