package br.com.api.consulta.teste.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import br.com.api.consulta.teste.model.Operadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperadoraRepository extends JpaRepository<Operadora, Long> {

    List<Operadora> findTop10ByOrderByDespesaSaudeDesc();

    @Query("SELECT o FROM Operadora o JOIN o.demonstracoes d " +
            "WHERE d.data BETWEEN :startDate AND :endDate " +
            "GROUP BY o.id, o.nome, o.cnpj " +
            "ORDER BY SUM(d.despesaHospitalar + d.despesaAmbulatorial) DESC")
    Page<Operadora> findTop10ByDespesaSaudeInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    Optional<Operadora> findByCnpj(String cnpj);

    @Query(value = """
        SELECT o.nome, SUM(d.despesa_hospitalar) as total 
        FROM demonstracoes_contabeis d 
        JOIN operadoras o ON d.operadora_id = o.id 
        WHERE d.data BETWEEN :startDate AND :endDate 
        GROUP BY o.nome 
        ORDER BY total DESC 
        LIMIT 10""", nativeQuery = true)
    List<Object[]> findTop10ByDespesaHospitalarPeriodo(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Consulta para contar por UF
    @Query("SELECT COUNT(o) FROM Operadora o WHERE o.uf = :uf")
    long countByUf(@Param("uf") String uf);
}
