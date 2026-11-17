package br.edu.nassau.asordens.service;

import br.edu.nassau.asordens.dto.ChamadoResponseDTO;
import br.edu.nassau.asordens.model.StatusChamado;
import br.edu.nassau.asordens.repository.ChamadoRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {
    private final ChamadoRepository repository;

    public List<ChamadoResponseDTO> chamados(LocalDate inicio, LocalDate fim, StatusChamado status, Long equipeId) {
        var dataInicio = inicio != null ? inicio : LocalDate.now().minusDays(30);
        var dataFim = fim != null ? fim : LocalDate.now();
        if (dataFim.isBefore(dataInicio)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O fim deve ser posterior ao início.");
        return repository.buscarRelatorio(dataInicio.atStartOfDay().atOffset(ZoneOffset.UTC),
                dataFim.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC), status, equipeId)
                .stream().map(ChamadoResponseDTO::fromEntity).toList();
    }

    public List<Map<String, Object>> porCategoria() {
        return repository.contarPorCategoria().stream().map(linha -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoria", linha[0]);
            item.put("total", linha[1]);
            return item;
        }).toList();
    }
}
