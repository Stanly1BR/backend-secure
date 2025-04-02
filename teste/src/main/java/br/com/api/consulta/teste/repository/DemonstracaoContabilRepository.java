package br.com.api.consulta.teste.repository;


import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.model.DemonstracaoContabil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DemonstracaoContabilRepository extends JpaRepository<DemonstracaoContabil, Long> {

    List<DemonstracaoContabil> findByCnpj(String cnpj);

    List<DemonstracaoContabil> findByDataBetween(LocalDate startDate, LocalDate endDate);

    List<DemonstracaoContabil> findByAnoReferencia(Integer ano);

    List<DemonstracaoContabil> findByOperadoraId(Long operadoraId);

    List<DemonstracaoContabil> findByAnoReferenciaAndDespesaHospitalarGreaterThan(Integer ano, BigDecimal valor);

    @Query("SELECT d.operadora, SUM(d.despesaHospitalar) FROM DemonstracaoContabil d " +
            "WHERE d.anoReferencia = :ano " +
            "GROUP BY d.operadora " +
            "ORDER BY SUM(d.despesaHospitalar) DESC")
    List<Object[]> findTotalDespesasHospitalaresPorOperadora(@Param("ano") Integer ano);

    @Query(value = "SELECT o.razao_social, SUM(d.despesa_hospitalar) as total " +
            "FROM demonstracoes_contabeis d " +
            "JOIN operadoras o ON d.operadora_id = o.id " +
            "WHERE d.ano_referencia = :ano " +
            "GROUP BY o.razao_social " +
            "ORDER BY total DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopOperadorasPorDespesa(@Param("ano") Integer ano, @Param("limit") int limit);

    @Query("SELECT d FROM DemonstracaoContabil d WHERE d.operadora = :operadora ORDER BY d.data ASC")
    List<DemonstracaoContabil> findByOperadoraOrdenadoPorData(@Param("operadora") Operadora operadora);
}
