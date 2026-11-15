package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
