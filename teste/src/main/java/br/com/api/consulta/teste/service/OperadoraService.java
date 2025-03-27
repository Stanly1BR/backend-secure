package br.com.api.consulta.teste.service;

import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.repository.OperadoraRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OperadoraService {
    private final OperadoraRepository operadoraRepository;

    public OperadoraService(OperadoraRepository operadoraRepository) {
        this.operadoraRepository = operadoraRepository;
    }
    public List<Operadora> getTop10OperadorasComMaioresDespesas(){
        return operadoraRepository.findTop10ByOrderByDespesaSaudeDesc();
    }
    public List<Operadora> getTop10OperadorasNoPeriodo(LocalDate startDate, LocalDate endDate) {
        return operadoraRepository.findTop10ByDespesaSaudeInPeriod(startDate, endDate);
    }
}
