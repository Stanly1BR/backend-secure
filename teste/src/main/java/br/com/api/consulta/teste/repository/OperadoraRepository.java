package br.com.api.consulta.teste.repository;

import br.com.api.consulta.teste.model.Operadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OperadoraRepository extends JpaRepository<Operadora, Long> {

    List<Operadora> findTop10ByOrderByDespesaSaudeDesc();

    @Query("SELECT o FROM Operadora o WHERE o.data BETWEEN :startDate AND :endDate ORDER BY o.despesaSaude DESC")
    List<Operadora> findTop10ByDespesaSaudeInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
