package br.com.api.consulta.teste.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operadoras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operadora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 14, unique = true, nullable = false)
    private String cnpj;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(name = "nome_fantasia", length = 255)
    private String nomeFantasia;

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro;

    @Column(nullable = false, length = 100)
    private String modalidade;

    @Column(length = 255)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 8)
    private String cep;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @OneToMany(mappedBy = "operadora", fetch = FetchType.EAGER)
    private List<DemonstracaoContabil> demonstracoes = new ArrayList<>();

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    private LocalDateTime dataAtualizacao;

    @Column(name = "despesa_saude", precision = 15, scale = 2)
    private BigDecimal despesaSaude = BigDecimal.ZERO;

    public void atualizarDespesa(BigDecimal valor) {
        this.despesaSaude = valor != null ? valor : BigDecimal.ZERO;
        this.dataAtualizacao = LocalDateTime.now();
    }
}

