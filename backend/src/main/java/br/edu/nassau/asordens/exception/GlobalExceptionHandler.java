package br.edu.nassau.asordens.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RecursoNaoEncontradoException.class, EntityNotFoundException.class})
    public ResponseEntity handleNotFound(RuntimeException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity handleRegraNegocioException(RegraNegocioException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Não é possível excluir este registro porque ele possui dados vinculados.", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidation(MethodArgumentNotValidException ex) {
        var erros = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of(
                        "campo", err.getField(),
                        "mensagem", err.getDefaultMessage() != null ? err.getDefaultMessage() : "Valor inválido"
                ))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação nos campos enviados.", erros);
    }

    @ExceptionHandler({IllegalArgumentException.class, org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity handleInvalidInput(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado para o seu perfil.", null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildResponse(status, ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado no servidor.", null);
    }

    private ResponseEntity buildResponse(HttpStatus status, String mensagem, Object detalhes) {
        var body = new LinkedHashMap();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("erro", mensagem);
        if (detalhes != null) {
            body.put("detalhes", detalhes);
        }
        return ResponseEntity.status(status).body(body);
    }
}
