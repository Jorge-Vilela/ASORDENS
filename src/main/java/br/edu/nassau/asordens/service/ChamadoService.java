package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.model.Model;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class ChamadoService {
    @Autowired
    private ChamadoRepository repository;

    public Model criarChamado(Model model) {
        model.setStatus("ABERTO");
        model.setDataAbertura(LocalDateTime.now());
        return repository.save(model);
    }
    public List<Model> listarTodos() {
        return repository.findAll();
    }

}
