package br.edu.nassau.asordens.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PrioridadeChamado {
    ALTA,
    MEDIA,
    BAIXA;

    @JsonCreator
    public static PrioridadeChamado fromString(String value) {
        return value == null ? null : PrioridadeChamado.valueOf(value.trim().toUpperCase());
    }
}
