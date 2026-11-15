package br.edu.nassau.asordens.categoria;

import br.edu.nassau.asordens.categoria.dto.CategoriaRequestDTO;
import br.edu.nassau.asordens.categoria.dto.CategoriaResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNomeIgnoreCase(dto.nome())){
            throw new IllegalArgumentException("Já existe uma categoria com este nome.");
        }
        Categoria categoria = Categoria.builder()
                .nome(dto.nome())
                .horas(dto.horas())
                .build();
        return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponseDTO::fromEntity)
                .toList();
    }
    @Transactional
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarEntidade(id);
        categoria.setNome(dto.nome());
        categoria.setHoras(dto.horas());
        return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
    }
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        categoriaRepository.delete(categoria);
    }
    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com este ID: " + id));
    }
}
