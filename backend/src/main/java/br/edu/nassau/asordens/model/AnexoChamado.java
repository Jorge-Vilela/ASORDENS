package br.edu.nassau.asordens.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_anexos_chamado")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnexoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipo;

    private long tamanho;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;
}
