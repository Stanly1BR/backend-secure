package br.com.api.consulta.teste.service;


import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.model.DemonstracaoContabil;
import br.com.api.consulta.teste.repository.OperadoraRepository;
import br.com.api.consulta.teste.repository.DemonstracaoContabilRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperadoraAnalysisService {
    private final OperadoraRepository operadoraRepository;
    private final DemonstracaoContabilRepository demonstracaoRepository;

    // Método para o relatório top10
    public List<OperadoraDespesaDTO> getTop10OperadorasUltimoTrimestre() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);

        return operadoraRepository.findTop10ByDespesaHospitalarPeriodo(startDate, endDate)
                .stream()
                .map(result -> new OperadoraDespesaDTO(
                        (String) result[0],
                        (BigDecimal) result[1]))
                .collect(Collectors.toList());
    }

    // ========== CONSULTAS BÁSICAS ==========

    /**
     * Obtém as 10 operadoras com maiores despesas
     */
    public List<Operadora> findTop10OperadorasByDespesa() {
        return operadoraRepository.findTop10ByOrderByDespesaSaudeDesc();
    }

    /**
     * Obtém as 10 operadoras com maiores despesas em um período
     */
    public List<Operadora> findTop10OperadorasByDespesaPeriodo(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return operadoraRepository.findTop10ByDespesaSaudeInPeriod(startDate, endDate, PageRequest.of(0, 10))
                .getContent();
    }

    // ========== ANÁLISES AVANÇADAS ==========

    /**
     * Calcula o crescimento percentual das despesas por operadora em um período
     */
    public Map<String, BigDecimal> calcularCrescimentoDespesas(LocalDate inicio, LocalDate fim) {
        validateDateRange(inicio, fim);

        return demonstracaoRepository.findByDataBetween(inicio, fim).stream()
                .collect(Collectors.groupingBy(
                        dc -> dc.getOperadora().getNome(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calcularVariacaoPeriodo
                        )
                ));
    }

    /**
     * Identifica operadoras com maior variação de despesas
     */
    public List<Operadora> findOperadorasComMaiorVariacao(int limite) {
        return operadoraRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        op -> calcularVariacaoDespesas(op).abs(),
                        Comparator.reverseOrder()
                ))
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Calcula a média de despesas por UF
     */
    public Map<String, BigDecimal> calcularMediaDespesasPorUF() {
        return operadoraRepository.findAll().stream()
                .filter(op -> op.getUf() != null && !op.getUf().isEmpty())
                .collect(Collectors.groupingBy(
                        Operadora::getUf,
                        Collectors.mapping(
                                op -> new BigDecimal(op.getDespesaSaude().toString()),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        BigDecimal::add
                                )
                        )
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().divide(
                                new BigDecimal(operadoraRepository.countByUf(e.getKey())),
                                2, RoundingMode.HALF_UP
                        )
                ));
    }

    /**
     * Gera relatório consolidado por modalidade
     */
    public Map<String, BigDecimal> gerarRelatorioPorModalidade() {
        return operadoraRepository.findAll().stream()
                .filter(op -> op.getModalidade() != null && !op.getModalidade().isEmpty())
                .collect(Collectors.groupingBy(
                        Operadora::getModalidade,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                op -> new BigDecimal(op.getDespesaSaude().toString()),
                                BigDecimal::add
                        )
                ));
    }

    // ========== MÉTODOS PRIVADOS ==========

    private BigDecimal calcularVariacaoDespesas(Operadora operadora) {
        List<DemonstracaoContabil> demonstracoes = demonstracaoRepository
                .findByOperadoraOrdenadoPorData(operadora);

        if (demonstracoes.size() < 2) {
            return BigDecimal.ZERO;
        }

        return calcularVariacao(
                demonstracoes.get(0).getTotalDespesas(),
                demonstracoes.get(demonstracoes.size()-1).getTotalDespesas()
        );
    }

    private BigDecimal calcularVariacaoPeriodo(List<DemonstracaoContabil> demonstracoes) {
        if (demonstracoes.size() < 2) {
            return BigDecimal.ZERO;
        }

        demonstracoes.sort(Comparator.comparing(DemonstracaoContabil::getData));
        return calcularVariacao(
                demonstracoes.get(0).getTotalDespesas(),
                demonstracoes.get(demonstracoes.size()-1).getTotalDespesas()
        );
    }

    private BigDecimal calcularVariacao(BigDecimal inicio, BigDecimal fim) {
        if (inicio.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return fim.subtract(inicio)
                .divide(inicio, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
    }
    @Getter
    @AllArgsConstructor
    public static class OperadoraDespesaDTO {
        private String operadora;
        private BigDecimal totalDespesa;
    }
}