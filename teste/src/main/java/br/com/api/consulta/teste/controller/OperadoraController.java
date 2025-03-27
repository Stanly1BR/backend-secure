package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.service.OperadoraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/operadoras")
public class OperadoraController {
    private final OperadoraService operadoraService;
    public OperadoraController(OperadoraService operadoraService) {
        this.operadoraService = operadoraService;
    }
    @GetMapping("/top10")
    public List<Operadora> getTop10Operadoras(){
        return operadoraService.getTop10OperadorasComMaioresDespesas();
    }
    @GetMapping("/top10-por-periodo")
    public List<Operadora> getTop10OperadorasPorPeriodo(@RequestParam String startDate, @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return operadoraService.getTop10OperadorasNoPeriodo(start, end);
    }
}
