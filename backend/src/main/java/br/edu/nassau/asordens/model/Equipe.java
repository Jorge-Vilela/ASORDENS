package br.edu.nassau.asordens.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
