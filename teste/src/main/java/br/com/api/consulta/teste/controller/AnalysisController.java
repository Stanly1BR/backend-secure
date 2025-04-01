package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.NovosService.OperadoraAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/analises")
public class AnalysisController {
    private final OperadoraAnalysisService analysisService;

    public AnalysisController(OperadoraAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/crescimento-despesas")
    public ResponseEntity<Map<String, BigDecimal>> getCrescimentoDespesas(
            @RequestParam String inicio,
            @RequestParam String fim) {
        return ResponseEntity.ok(
                analysisService.calcularCrescimentoDespesas(
                        LocalDate.parse(inicio),
                        LocalDate.parse(fim))
        );
    }

    @GetMapping("/maiores-variacoes")
    public ResponseEntity<?> getMaioresVariacoes(@RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(analysisService.identificarOperadorasComMaiorVariacao(limite));
    }

    @GetMapping("/media-por-uf")
    public ResponseEntity<Map<String, BigDecimal>> getMediaPorUF() {
        return ResponseEntity.ok(analysisService.calcularMediaDespesasPorUF());
    }

    @GetMapping("/relatorio-modalidade")
    public ResponseEntity<Map<String, BigDecimal>> getRelatorioPorModalidade() {
        return ResponseEntity.ok(analysisService.gerarRelatorioPorModalidade());
    }
}
