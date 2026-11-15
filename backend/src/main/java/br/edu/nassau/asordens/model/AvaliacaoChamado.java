package br.edu.nassau.asordens.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_avaliacoes_chamado")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AvaliacaoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer nota;

    @Column(length = 500)
    private String comentario;

    private OffsetDateTime dataAvaliacao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false, unique = true)
    private Chamado chamado;
}
