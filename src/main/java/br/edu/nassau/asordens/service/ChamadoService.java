package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.chamado.Chamado;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class ChamadoService {
    @Autowired
    private ChamadoRepository repository;

    public Chamado criarChamado(Chamado chamado) {
        chamado.setStatus("ABERTO");
        chamado.setDataAbertura(LocalDateTime.now());
        return repository.save(chamado);
    }
    public List<Chamado> listarTodos() {
        return repository.findAll();
    }

}
