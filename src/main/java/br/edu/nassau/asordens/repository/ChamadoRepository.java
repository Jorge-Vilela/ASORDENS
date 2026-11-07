package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Model, Long> {

    List<Model> findByStatus(String status);
}
