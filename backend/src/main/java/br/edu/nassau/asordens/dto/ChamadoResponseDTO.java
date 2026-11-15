package br.edu.nassau.asordens.dto;

import br.edu.nassau.asordens.model.Chamado;

import java.time.OffsetDateTime;
import java.util.List;

public record ChamadoResponseDTO(
        Long id,
        String titulo,
        String descricao,
        String status,
        String prioridade,
        OffsetDateTime dataAbertura,
        OffsetDateTime atualizadoEm,
        OffsetDateTime resolvidoEm,
        Long solicitanteId,
        String solicitanteNome,
        Long agenteId,
        String agenteNome,
        Long equipeId,
        String equipeNome,
        Long categoriaId,
        String categoriaNome,
        List<AnexoDTO> anexos,
        List<InteracaoResponseDTO> mensagens,
        AvaliacaoResponseDTO avaliacao
){
        public record InteracaoResponseDTO(Long id, String autor, String texto, OffsetDateTime enviadoEm){}
        public record AvaliacaoResponseDTO(Long id, Integer nota, String comentario, OffsetDateTime dataAvaliacao){}

    public static  ChamadoResponseDTO fromEntity(Chamado chamado){
            return new ChamadoResponseDTO(
                    chamado.getId(),
                    chamado.getTitulo(),
                    chamado.getDescricao(),
                    chamado.getStatus().name().toLowerCase(),
                    chamado.getPrioridade().name().toLowerCase(),
                    chamado.getDataAbertura(),
                    chamado.getAtualizadoEm(),
                    chamado.getResolvidoEm(),
                    chamado.getSolicitante().getId(),
                    chamado.getSolicitante().getNome(),
                    chamado.getAgente() != null ? chamado.getAgente().getId() : null,
                    chamado.getAgente() != null ? chamado.getAgente().getNome() : null,
                    chamado.getEquipe() != null ? chamado.getEquipe().getId() : null,
                    chamado.getEquipe() != null ? chamado.getEquipe().getNome() : null,
                    chamado.getCategoria().getId(),
                    chamado.getCategoria().getNome(),
                    chamado.getAnexos().stream().map(a -> new AnexoDTO(a.getId(), a.getNome(), a.getTipo(), a.getTamanho(), a.getUrl())).toList(),
                    chamado.getInteracoes().stream().map(i -> new InteracaoResponseDTO(i.getId(), i.getAutor().getNome(), i.getTexto(), i.getEnviadoEm())).toList(),
                    chamado.getAvaliacao() != null ? new AvaliacaoResponseDTO(chamado.getAvaliacao().getId(), chamado.getAvaliacao().getNota(), chamado.getAvaliacao().getComentario(), chamado.getAvaliacao().getDataAvaliacao()) : null
            );
    }
}
