package br.com.api.consulta.teste.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demonstracoes_contabeis",
        uniqueConstraints = @UniqueConstraint(columnNames = {"operadora_id", "data"}, name = "uk_demonstracao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemonstracaoContabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 14, nullable = false)
    private String cnpj;

    @ManyToOne(optional = false)
    @JoinColumn(name = "operadora_id", nullable = false, foreignKey = @ForeignKey(name = "fk_demonstracao_operadora"))
    private Operadora operadora;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "ano_referencia", nullable = false)
    private Integer anoReferencia;

    @Column(name = "despesa_hospitalar", nullable = false, precision = 15, scale = 2)
    private BigDecimal despesaHospitalar = BigDecimal.ZERO;

    @Column(name = "despesa_ambulatorial", nullable = false, precision = 15, scale = 2)
    private BigDecimal despesaAmbulatorial = BigDecimal.ZERO;

    @Column(name = "despesa_odontologica", precision = 15, scale = 2)
    private BigDecimal despesaOdontologica = BigDecimal.ZERO;

    @Column(name = "receita_operacional", precision = 15, scale = 2)
    private BigDecimal receitaOperacional = BigDecimal.ZERO;

    @Column(name = "lucro_liquido", precision = 15, scale = 2)
    private BigDecimal lucroLiquido = BigDecimal.ZERO;

    @Column(name = "data_envio")
    @CreationTimestamp
    private LocalDateTime dataEnvio;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(length = 20)
    private String status = "PENDENTE";

    public BigDecimal getTotalDespesas() {
        BigDecimal total = BigDecimal.ZERO;
        if (despesaHospitalar != null) total = total.add(despesaHospitalar);
        if (despesaAmbulatorial != null) total = total.add(despesaAmbulatorial);
        if (despesaOdontologica != null) total = total.add(despesaOdontologica);
        return total;
    }

    public void marcarComoAprovada() {
        this.status = "APROVADA";
        this.dataAprovacao = LocalDateTime.now();
    }
}
