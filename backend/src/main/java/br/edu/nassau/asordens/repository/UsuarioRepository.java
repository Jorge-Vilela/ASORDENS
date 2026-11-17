package br.edu.nassau.asordens.repository;

import br.edu.nassau.asordens.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Usuario> findByEquipeId(Long equipeId, Pageable pageable);
    java.util.List<Usuario> findByAtivoTrueAndPerfilIn(java.util.Collection<br.edu.nassau.asordens.model.Perfil> perfis);
}
