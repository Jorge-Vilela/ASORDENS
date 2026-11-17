package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.model.InteracaoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteracaoChamadoRepository extends JpaRepository<InteracaoChamado, Long> {
    boolean existsByAutorId(Long autorId);
}
