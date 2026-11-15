package br.edu.nassau.asordens.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
