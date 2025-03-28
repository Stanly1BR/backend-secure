package br.com.api.consulta.teste.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Entity
@Table(name = "operadoras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operadora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cnpj", unique = true, nullable = false)
    private String cnpj;

    @Column(name = "despesa_saude", nullable = false)
    private Double despesaSaude;

    @Column(name = "data", nullable = false)
    private LocalDate data;
}

