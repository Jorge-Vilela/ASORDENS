package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.exception.RegraNegocioException;
import br.edu.nassau.asordens.model.Equipe;
import br.edu.nassau.asordens.model.Perfil;
import br.edu.nassau.asordens.model.Usuario;
import br.edu.nassau.asordens.repository.UsuarioRepository;
import br.edu.nassau.asordens.dto.UsuarioRequestDTO;
import br.edu.nassau.asordens.dto.UsuarioResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EquipeService equipeService;
    private final PasswordEncoder passwordEncoder;
    private final br.edu.nassau.asordens.repository.ChamadoRepository chamadoRepository;
    private final br.edu.nassau.asordens.repository.InteracaoChamadoRepository interacaoRepository;
    private final br.edu.nassau.asordens.repository.ConfiguracaoRepository configuracaoRepository;

    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> listarAgentes() {
        return usuarioRepository.findByAtivoTrueAndPerfilIn(java.util.List.of(Perfil.AGENTE, Perfil.ADMIN)).stream()
                .map(u -> java.util.Map.<String, Object>of("id", u.getId(), "nome", u.getNome(), "email", u.getEmail(),
                        "perfil", u.getPerfil().name(), "equipe", u.getEquipe() != null ? u.getEquipe().getNome() : "Sem equipe"))
                .toList();
    }

    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema. ");
        }
        Equipe equipe = dto.equipeId() != null ? equipeService.buscarEntidade(dto.equipeId()) : null;

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil() != null ? dto.perfil() : Perfil.USER)
                .ativo(true)
                .equipe(equipe)
                .build();

        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarPaginado(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponseDTO alterarPerfil(Long id, Perfil novoPerfil) {
        Usuario usuario = buscarEntidade(id);
        usuario.setPerfil(novoPerfil);
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO vincularEquipe(Long id, Long equipeId) {
        Usuario usuario = buscarEntidade(id);
        Equipe equipe = equipeId != null ? equipeService.buscarEntidade(equipeId) : null;
        usuario.setEquipe(equipe);
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, br.edu.nassau.asordens.dto.UsuarioAtualizacaoDTO dto) {
        Usuario u = buscarEntidade(id);
        usuarioRepository.findByEmail(dto.email()).filter(outro -> !outro.getId().equals(id)).ifPresent(outro -> { throw new RegraNegocioException("E-mail ja cadastrado."); });
        u.setNome(dto.nome()); u.setEmail(dto.email()); u.setPerfil(dto.perfil()); u.setAtivo(dto.ativo());
        u.setEquipe(dto.equipeId() == null ? null : equipeService.buscarEntidade(dto.equipeId()));
        if (dto.senha() != null && !dto.senha().isBlank()) u.setSenha(passwordEncoder.encode(dto.senha()));
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(u));
    }
    @Transactional
    public void desativar(Long id) { Usuario u = buscarEntidade(id); u.setAtivo(false); usuarioRepository.save(u); }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarEntidade(id);
        var autenticacao = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && usuario.getEmail().equalsIgnoreCase(autenticacao.getName())) {
            throw new RegraNegocioException("Você não pode excluir sua própria conta.");
        }
        if (chamadoRepository.existsBySolicitanteIdOrAgenteId(id, id) || interacaoRepository.existsByAutorId(id)) {
            throw new RegraNegocioException("Este usuário possui chamados ou interações vinculados. Desative a conta para preservar o histórico.");
        }
        configuracaoRepository.findById(id).ifPresent(configuracaoRepository::delete);
        configuracaoRepository.flush();
        usuarioRepository.delete(usuario);
        usuarioRepository.flush();
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
    }
}
