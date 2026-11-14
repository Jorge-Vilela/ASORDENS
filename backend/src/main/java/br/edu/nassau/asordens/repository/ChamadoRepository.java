package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.chamado.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findByStatus(String status);
    boolean existsByCategoria(String categoria);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Chamado c where c.id = :id")
    java.util.Optional<Chamado> buscarParaAtualizar(@org.springframework.data.repository.query.Param("id") Long id);
}
