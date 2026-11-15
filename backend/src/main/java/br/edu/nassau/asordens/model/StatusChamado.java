package br.edu.nassau.asordens.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatusChamado {
    ABERTO,
    EM_ANDAMENTO,
    RESOLVIDO,
    FECHADO;

    @JsonCreator
    public static StatusChamado fromString(String value) {
        return value == null ? null : StatusChamado.valueOf(value.trim().toUpperCase());
    }
}
