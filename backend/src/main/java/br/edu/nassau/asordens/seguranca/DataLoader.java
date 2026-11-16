package br.edu.nassau.asordens.seguranca;

import br.edu.nassau.asordens.model.Categoria;
import br.edu.nassau.asordens.model.Equipe;
import br.edu.nassau.asordens.model.Perfil;
import br.edu.nassau.asordens.model.Usuario;
import br.edu.nassau.asordens.repository.CategoriaRepository;
import br.edu.nassau.asordens.repository.EquipeRepository;
import br.edu.nassau.asordens.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EquipeRepository equipeRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args)  {
        if (usuarioRepository.count() == 0){
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("admin@asordens.com");
            admin.setSenha(passwordEncoder.encode("Admin123"));
            admin.setPerfil(Perfil.ADMIN);
            admin.setAtivo(true);
            usuarioRepository.save(admin);

            Usuario agente = new Usuario();
            agente.setNome("Técnico Suporte");
            agente.setEmail("agente@asordens.com");
            agente.setSenha(passwordEncoder.encode("Agente123"));
            agente.setPerfil(Perfil.AGENTE);
            agente.setAtivo(true);
            usuarioRepository.save(agente);
        }
        if (equipeRepository.count() == 0){
            Equipe suporte = new Equipe();
            suporte.setNome("Suporte 1");
            suporte.setDescricao("Atendimento inicial e triagem");
            suporte.setAtivo(true);
            equipeRepository.save(suporte);
        }
        if (categoriaRepository.count() == 0){
            Categoria rede = new Categoria();
            rede.setNome("Rede");
            rede.setHoras(4);
            categoriaRepository.save(rede);

            Categoria software = new Categoria();
            software.setNome("Software");
            software.setHoras(8);
            categoriaRepository.save(software);
        }
    }
}
