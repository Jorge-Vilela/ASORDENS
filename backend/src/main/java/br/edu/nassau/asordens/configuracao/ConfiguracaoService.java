package br.edu.nassau.asordens.configuracao;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService implements ApplicationRunner {
    private final ConfiguracaoRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        if (!repository.existsById(1L)) repository.save(new Configuracao());
    }

    public Configuracao atual() { return repository.findById(1L).orElseThrow(); }
    public Configuracao salvar(Configuracao config) { return repository.save(config); }
}
