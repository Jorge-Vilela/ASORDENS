package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.chamado.Chamado;
import br.edu.nassau.asordens.chamado.Chamado.Anexo;
import br.edu.nassau.asordens.chamado.Chamado.Mensagem;
import br.edu.nassau.asordens.configuracao.ConfiguracaoService;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ChamadoService {
    private final ChamadoRepository repository;
    private final ConfiguracaoService configuracao;
    private static final Set<String> STATUS = Set.of("aberto", "em_andamento", "resolvido", "fechado");

    public Chamado criarChamado(Chamado dados) {
        var chamado = new Chamado();
        copiarDados(chamado, dados);
        chamado.setStatus("aberto");
        chamado.setDataAbertura(OffsetDateTime.now());
        return salvar(chamado);
    }

    @Transactional(readOnly = true)
    public List<Chamado> listarTodos() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Chamado buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    private Chamado bloquear(Long id) {
        return repository.buscarParaAtualizar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    public Chamado atualizarChamado(Long id, Chamado dados) {
        var chamado = bloquear(id);
        copiarDados(chamado, dados);
        return salvar(chamado);
    }

    private void copiarDados(Chamado chamado, Chamado dados) {
        if (dados.getTitulo() == null || dados.getTitulo().isBlank() || dados.getTitulo().length() > 255
            || dados.getSolicitante() == null || dados.getSolicitante().isBlank() || dados.getSolicitante().length() > 255)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe título e solicitante (até 255 caracteres).");
        if (dados.getCategoria() == null || !configuracao.atual().getCategorias().contains(dados.getCategoria()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida.");
        if (dados.getPrioridade() == null || !Set.of("alta", "media", "baixa").contains(dados.getPrioridade()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prioridade inválida.");
        validarAnexos(dados.getAnexos());
        chamado.setTitulo(dados.getTitulo().trim());
        chamado.setSolicitante(dados.getSolicitante().trim());
        chamado.setDescricao(dados.getDescricao());
        chamado.setCategoria(dados.getCategoria());
        chamado.setPrioridade(dados.getPrioridade());
        chamado.setAnexos(dados.getAnexos() == null ? new ArrayList<>() : new ArrayList<>(dados.getAnexos()));
    }

    public Chamado atualizarStatus(Long id, String status) {
        if (status == null || !STATUS.contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido.");
        var chamado = bloquear(id);
        if (Set.of("resolvido", "fechado").contains(status)) {
            if (chamado.getResolvidoEm() == null) chamado.setResolvidoEm(OffsetDateTime.now());
        } else chamado.setResolvidoEm(null);
        chamado.setStatus(status);
        return salvar(chamado);
    }

    public Chamado puxar(Long id) {
        var chamado = bloquear(id);
        if (chamado.getAgente() != null && !chamado.getAgente().isBlank())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este chamado já foi atribuído a um agente.");
        if (!"aberto".equals(chamado.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente chamados abertos podem ser puxados.");
        chamado.setAgente(configuracao.atual().getNome());
        chamado.setStatus("em_andamento");
        return salvar(chamado);
    }

    public Chamado enviarMensagem(Long id, Mensagem dados) {
        var chamado = bloquear(id);
        validarAnexos(dados.getAnexos());
        if ((dados.getTexto() == null || dados.getTexto().isBlank()) && (dados.getAnexos() == null || dados.getAnexos().isEmpty()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma mensagem ou um anexo.");
        var mensagem = new Mensagem();
        mensagem.setId(UUID.randomUUID().toString());
        mensagem.setAutor("agente");
        mensagem.setNome(configuracao.atual().getNome());
        mensagem.setTexto(dados.getTexto() == null ? "" : dados.getTexto().trim());
        mensagem.setAnexos(dados.getAnexos() == null ? new ArrayList<>() : dados.getAnexos());
        mensagem.setEnviadoEm(OffsetDateTime.now());
        var mensagens = chamado.getMensagens() == null ? new ArrayList<Mensagem>() : new ArrayList<>(chamado.getMensagens());
        mensagens.add(mensagem);
        chamado.setMensagens(mensagens);
        return salvar(chamado);
    }

    private void validarAnexos(List<Anexo> anexos) {
        if (anexos == null) return;
        if (anexos.size() > 10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo de 10 anexos por envio.");
        for (var anexo : anexos) {
            if (anexo == null || anexo.getNome() == null || anexo.getNome().isBlank() || anexo.getUrl() == null
                || !anexo.getUrl().startsWith("data:") || !anexo.getUrl().contains(";base64,"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anexo inválido.");
            try {
                var bytes = Base64.getDecoder().decode(anexo.getUrl().substring(anexo.getUrl().indexOf(";base64,") + 8));
                if (bytes.length > 10 * 1024 * 1024) throw new IllegalArgumentException();
                anexo.setTamanho(bytes.length);
                anexo.setId(anexo.getId() == null ? UUID.randomUUID().toString() : anexo.getId());
                if (anexo.getTipo() == null) anexo.setTipo("application/octet-stream");
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anexo inválido ou maior que 10 MB.");
            }
        }
    }

    private Chamado salvar(Chamado chamado) {
        chamado.setAtualizadoEm(OffsetDateTime.now());
        return repository.save(chamado);
    }

    public void deletarChamado(Long id) { repository.delete(bloquear(id)); }
}
