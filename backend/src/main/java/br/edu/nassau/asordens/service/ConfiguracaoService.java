package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.model.Configuracao;
import br.edu.nassau.asordens.repository.ConfiguracaoRepository;
import br.edu.nassau.asordens.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {
    private final ConfiguracaoRepository repository;
    private final UsuarioRepository usuarios;
    @Transactional
    public Configuracao atual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarios.findByEmail(email).orElseThrow();
        return repository.findById(usuario.getId()).orElseGet(() -> {
            var config = new Configuracao();
            config.setUsuario(usuario);
            config.setNome(usuario.getNome());
            config.setEmail(usuario.getEmail());
            config.setCargo("");
            config.setEquipe(usuario.getEquipe() == null ? "" : usuario.getEquipe().getNome());
            return repository.save(config);
        });
    }
    public Configuracao salvar(Configuracao config) { return repository.save(config); }
}
