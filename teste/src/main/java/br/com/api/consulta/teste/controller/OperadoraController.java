package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.service.OperadoraAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/operadoras")
public class OperadoraController {
    private final OperadoraAnalysisService operadoraAnalysisService;

    public OperadoraController(OperadoraAnalysisService operadoraAnalysisService) {
        this.operadoraAnalysisService = operadoraAnalysisService;
    }

    // ========== ENDPOINTS BÁSICOS ==========

    /**
     * Obtém as 10 operadoras com maiores despesas
     */
    @GetMapping("/top10")
    public ResponseEntity<List<Operadora>> getTop10Operadoras() {
        return ResponseEntity.ok(operadoraAnalysisService.findTop10OperadorasByDespesa());
    }

    /**
     * Obtém as 10 operadoras com maiores despesas em um período específico
     */
    @GetMapping("/top10-periodo")
    public ResponseEntity<?> getTop10OperadorasPorPeriodo(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return ResponseEntity.ok(operadoraAnalysisService.findTop10OperadorasByDespesaPeriodo(start, end));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Formato de data inválido. Use YYYY-MM-DD.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== ENDPOINTS ANALÍTICOS ==========

    /**
     * Calcula o crescimento percentual das despesas por operadora em um período
     */
    @GetMapping("/analises/crescimento-despesas")
    public ResponseEntity<?> getCrescimentoDespesas(
            @RequestParam String inicio,
            @RequestParam String fim) {
        try {
            LocalDate dataInicio = LocalDate.parse(inicio);
            LocalDate dataFim = LocalDate.parse(fim);
            return ResponseEntity.ok(operadoraAnalysisService.calcularCrescimentoDespesas(dataInicio, dataFim));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Formato de data inválido. Use YYYY-MM-DD.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Identifica operadoras com maior variação de despesas
     */
    @GetMapping("/analises/maiores-variacoes")
    public ResponseEntity<List<Operadora>> getOperadorasComMaiorVariacao(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(operadoraAnalysisService.findOperadorasComMaiorVariacao(limite));
    }

    /**
     * Calcula a média de despesas por UF
     */
    @GetMapping("/analises/media-por-uf")
    public ResponseEntity<Map<String, BigDecimal>> getMediaDespesasPorUF() {
        return ResponseEntity.ok(operadoraAnalysisService.calcularMediaDespesasPorUF());
    }

    /**
     * Gera relatório consolidado por modalidade
     */
    @GetMapping("/analises/relatorio-modalidade")
    public ResponseEntity<Map<String, BigDecimal>> getRelatorioPorModalidade() {
        return ResponseEntity.ok(operadoraAnalysisService.gerarRelatorioPorModalidade());
    }
}
