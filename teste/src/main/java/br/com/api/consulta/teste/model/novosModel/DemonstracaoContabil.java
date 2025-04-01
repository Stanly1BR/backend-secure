package br.com.api.consulta.teste.model.novosModel;

import br.com.api.consulta.teste.model.Operadora;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "demonstracoes_contabeis")
@Data
public class DemonstracaoContabil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cnpj", length = 14, nullable = false)
    private String cnpj;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "ano_referencia", nullable = false)
    private Integer anoReferencia;

    @Column(precision = 15, scale = 2)
    private BigDecimal despesaHospitalar;

    @Column(precision = 15, scale = 2)
    private BigDecimal despesaAmbulatorial;

    @ManyToOne
    @JoinColumn(name = "operadora_id")
    private Operadora operadora;

    public BigDecimal getTotalDespesas() {
        if (despesaHospitalar == null) despesaHospitalar = BigDecimal.ZERO;
        if (despesaAmbulatorial == null) despesaAmbulatorial = BigDecimal.ZERO;
        return despesaHospitalar.add(despesaAmbulatorial);
    }
}
