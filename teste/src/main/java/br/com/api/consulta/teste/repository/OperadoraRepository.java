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

    List<Operadora> findByOrderByDespesaSaudeDesc(Pageable pageable);

    @Query("SELECT o FROM Operadora o WHERE o.data BETWEEN :startDate AND :endDate ORDER BY o.despesaSaude DESC")
    Page<Operadora> findTop10ByDespesaSaudeInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    Optional<Operadora> findByCnpj(String cnpj);
}
