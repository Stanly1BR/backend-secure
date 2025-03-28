package br.com.api.consulta.teste.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "procedimentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Procedimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "ambulatorial", nullable = false)
    private String ambulatorial;

    @Column(name = "hospitalar", nullable = false)
    private String hospitalar;
}
