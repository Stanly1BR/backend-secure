package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.service.OperadoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
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
    public ResponseEntity<List<Operadora>> getTop10Operadoras(){
        return ResponseEntity.ok(operadoraService.getTop10OperadorasComMaioresDespesas());
    }

    @GetMapping("/top10-por-periodo")
    public ResponseEntity<?> getTop10OperadorasPorPeriodo(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (start.isAfter(end)) {
                return ResponseEntity.badRequest().body("A data inicial não pode ser posterior à data final.");
            }
            return  ResponseEntity.ok(operadoraService.getTop10OperadorasNoPeriodo(start,end));
        }catch (DateTimeException e){
            return ResponseEntity.badRequest().body("Formato de data inválido. Use o formato YYYY-MM-DD.");
        }
    }
}
