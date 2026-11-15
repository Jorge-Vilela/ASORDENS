package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.model.Equipe;
import br.edu.nassau.asordens.dto.EquipeRequestDTO;
import br.edu.nassau.asordens.dto.EquipeResponseDTO;
import br.edu.nassau.asordens.repository.EquipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;

    @Transactional
    public EquipeResponseDTO criar(EquipeRequestDTO dto) {
        if (equipeRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new IllegalArgumentException("Já existe uma equipe com este nome. ");
        }
        Equipe equipe = Equipe.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .ativo(dto.ativo() == null || dto.ativo())
                .build();
        return EquipeResponseDTO.fromEntity(equipeRepository.save(equipe));
    }

    @Transactional(readOnly = true)
    public List<EquipeResponseDTO> listarTodas() {
        return equipeRepository.findAll().stream()
                .map(EquipeResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipeResponseDTO buscarPorId(Long id) {
        return EquipeResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public EquipeResponseDTO atualizar(Long id, EquipeRequestDTO dto) {
        Equipe equipe = buscarEntidade(id);
        equipe.setNome(dto.nome());
        equipe.setDescricao(dto.descricao());
        if (dto.ativo() != null) {
            equipe.setAtivo(dto.ativo());
        }
        return EquipeResponseDTO.fromEntity(equipeRepository.save(equipe));
    }
    @Transactional
    public void excluir(Long id) {
        Equipe equipe = buscarEntidade(id);
        equipeRepository.delete(equipe);
    }

    public Equipe buscarEntidade(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Equipe não encontrada com ID: "+id));
    }
}