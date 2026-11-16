package br.edu.nassau.asordens.controller;

import br.edu.nassau.asordens.dto.ChamadoResponseDTO;
import br.edu.nassau.asordens.model.StatusChamado;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioController {

    private final ChamadoRepository chamadoRepository;

    @GetMapping("/chamados")
    public ResponseEntity<List<ChamadoResponseDTO>> relatorioChamados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Long equipeId) {

        OffsetDateTime dataInicio = (inicio != null ? inicio : LocalDate.now().minusDays(30))
                .atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime dataFim = (fim != null ? fim : LocalDate.now())
                .plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);

        List<ChamadoResponseDTO> resultado = chamadoRepository.findByDataAberturaBetween(dataInicio, dataFim)
                .stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> equipeId == null || (c.getEquipe() != null && c.getEquipe().getId().equals(equipeId)))
                .map(ChamadoResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(resultado);
    }
    @GetMapping("/por-categoria")
    public ResponseEntity chamadosPorCategoria() {
        var dados = chamadoRepository.contarPorCategoria().stream()
                .map(linha -> {
                    var item = new LinkedHashMap();
                    item.put("categoria", linha[0]);
                    item.put("total", linha[1]);
                    return item;
                })
                .toList();
        return ResponseEntity.ok(dados);
    }
}
