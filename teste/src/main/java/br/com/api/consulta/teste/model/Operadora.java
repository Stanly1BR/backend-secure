package br.com.api.consulta.teste.model;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "data_registro")
    private LocalDate dataRegistro;

    @Column(name = "modalidade")
    private String modalidade;

    @Column(name = "logradouro")
    private String logradouro;

    @Column(name = "uf")
    private String uf;

    @Column(name = "despesa_saude", nullable = false)
    private Double despesaSaude;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public void setDespesaSaude(Double despesaSaude) {
        this.despesaSaude = despesaSaude;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}

