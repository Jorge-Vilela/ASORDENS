package br.edu.nassau.asordens.dto;

import br.edu.nassau.asordens.model.PrioridadeChamado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChamadoRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String titulo,

        String descricao,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        Long equipeId,

        @NotNull(message = "A prioridade é obrigatória")
        PrioridadeChamado prioridade,

        @Valid
        List<AnexoDTO> anexos
){}
