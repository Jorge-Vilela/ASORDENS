package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.model.Chamado;
import br.edu.nassau.asordens.model.PrioridadeChamado;
import br.edu.nassau.asordens.model.StatusChamado;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Chamado c WHERE c.id = :id")
    Optional<Chamado> buscarParaAtualizar(@Param("id") Long id);

    Page<Chamado> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
    Page<Chamado> findByEquipeId(Long equipeId, Pageable pageable);
    Page<Chamado> findBySolicitanteId(Long solicitanteId, Pageable pageable);
    List<Chamado> findByDataAberturaBetween(OffsetDateTime inicio, OffsetDateTime fim);

    @Query("""
        SELECT c FROM Chamado c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:prioridade IS NULL OR c.prioridade = :prioridade)
          AND (:equipeId IS NULL OR c.equipe.id = :equipeId)
          AND (:titulo IS NULL OR LOWER(c.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
    """)
    Page<Chamado> buscarComFiltros(
            @Param("status") StatusChamado status,
            @Param("prioridade") PrioridadeChamado prioridade,
            @Param("equipeId") Long equipeId,
            @Param("titulo") String titulo,
            Pageable pageable
    );
            @Query("SELECT c.categoria.nome, COUNT(c) FROM Chamado c GROUP BY c.categoria.nome")
                    List<Object[]> contarPorCategoria();
}

