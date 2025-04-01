package br.com.api.consulta.teste.service.NovosService;

import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.model.novosModel.DemonstracaoContabil;
import br.com.api.consulta.teste.repository.OperadoraRepository;
import br.com.api.consulta.teste.repository.novosRepository.DemonstracaoContabilRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperadoraAnalysisService {
    private final OperadoraRepository operadoraRepository;
    private final DemonstracaoContabilRepository demonstracaoRepository;

    public OperadoraAnalysisService(OperadoraRepository operadoraRepository,
                                    DemonstracaoContabilRepository demonstracaoRepository) {
        this.operadoraRepository = operadoraRepository;
        this.demonstracaoRepository = demonstracaoRepository;
    }

    /**
     * Calcula o crescimento percentual das despesas de saúde por operadora em um período
     */
    public Map<String, BigDecimal> calcularCrescimentoDespesas(LocalDate inicio, LocalDate fim) {
        List<DemonstracaoContabil> demonstracoes = demonstracaoRepository.findByDataBetween(inicio, fim);

        return demonstracoes.stream()
                .collect(Collectors.groupingBy(
                        dc -> dc.getOperadora().getNome(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    if (list.size() < 2) return BigDecimal.ZERO;

                                    list.sort(Comparator.comparing(DemonstracaoContabil::getData));
                                    BigDecimal primeiro = list.get(0).getTotalDespesas();
                                    BigDecimal ultimo = list.get(list.size()-1).getTotalDespesas();

                                    if (primeiro.compareTo(BigDecimal.ZERO) == 0) {
                                        return BigDecimal.ZERO;
                                    }

                                    return ultimo.subtract(primeiro)
                                            .divide(primeiro, 4, BigDecimal.ROUND_HALF_UP)
                                            .multiply(new BigDecimal(100));
                                }
                        )
                ));
    }

    /**
     * Identifica operadoras com maior variação de despesas
     */
    public List<Operadora> identificarOperadorasComMaiorVariacao(int limite) {
        return operadoraRepository.findAll().stream()
                .sorted(Comparator.comparing(op ->
                        calcularVariacaoDespesas(op).abs(), Comparator.reverseOrder()))
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
                        Collectors.averagingDouble(Operadora::getDespesaSaude)
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> BigDecimal.valueOf(e.getValue())
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
                                op -> BigDecimal.valueOf(op.getDespesaSaude()),
                                BigDecimal::add
                        )
                ));
    }

    /**
     * Calcula a variação percentual das despesas para uma operadora específica
     */
    private BigDecimal calcularVariacaoDespesas(Operadora operadora) {
        List<DemonstracaoContabil> demonstracoes = demonstracaoRepository.findByOperadoraOrdenadoPorData(operadora);

        if (demonstracoes.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal primeiro = demonstracoes.get(0).getTotalDespesas();
        BigDecimal ultimo = demonstracoes.get(demonstracoes.size()-1).getTotalDespesas();

        return ultimo.subtract(primeiro)
                .divide(primeiro, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
    }
}
