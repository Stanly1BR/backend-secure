package br.com.api.consulta.teste.repository;

import br.com.api.consulta.teste.model.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcedimentoRepository extends JpaRepository<Procedimento, Long> {
    Optional<Procedimento> findByCodigo(String codigo);
}

