package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipeRepository extends JpaRepository<Equipe,Long> {
    boolean existsByNomeIgnoreCase(String nome);
    List<Equipe> findByAtivoTrue();
}
