package br.edu.nassau.asordens.chamado;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "tb_chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String status;

    private OffsetDateTime dataAbertura;
    private String solicitante;
    private String categoria;
    private String prioridade;
    private String agente;
    private OffsetDateTime atualizadoEm;
    private OffsetDateTime resolvidoEm;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Anexo> anexos = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Mensagem> mensagens = new ArrayList<>();

    @Data
    public static class Anexo {
        private String id;
        private String nome;
        private String tipo;
        private long tamanho;
        private String url;
    }

    @Data
    public static class Mensagem {
        private String id;
        private String autor;
        private String nome;
        private String texto;
        private List<Anexo> anexos = new ArrayList<>();
        private OffsetDateTime enviadoEm;
    }
}
