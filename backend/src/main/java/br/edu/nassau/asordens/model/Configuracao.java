package br.edu.nassau.asordens.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.*;

@Data
@Entity
@Table(name = "tb_configuracao")
public class Configuracao {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;
    private String nome = "Andre Moura";
    private String cargo = "Agente de TI";
    private String equipe = "Suporte";
    private String email = "";
    private String telefone = "";
    private String disponibilidade = "online";
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> preferencias = new HashMap<>(Map.of(
        "notificarNovoChamado", true, "notificarRespostaSolicitante", true, "notificarPorEmail", false));
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> categorias = new ArrayList<>(List.of("Sistemas", "Hardware", "Acessos", "Rede", "E-mail", "SO"));
}
