package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.DataImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/ans")
public class AnsDataController {
    private final DataImportService dataImportService;

    public AnsDataController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @GetMapping("/baixar-dados")
    public ResponseEntity<String> baixarDadosCompletos() {
        try {
            // Fluxo completo de download
            dataImportService.importarOperadoras();
            dataImportService.importarDemonstracoesContabeis();

            return ResponseEntity.ok("Todos os dados da ANS foram baixados e extraídos com sucesso!");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar dados da ANS: " + e.getMessage());
        }
    }

    @PostMapping("/importar-operadoras")
    public ResponseEntity<String> importarOperadoras() {
        try {
            dataImportService.importarOperadoras();
            return ResponseEntity.ok("Operadoras importadas com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao importar operadoras: " + e.getMessage());
        }
    }

    @GetMapping("/baixar-demonstrativos")
    public ResponseEntity<String> baixarDemonstrativos() {
        try {
            dataImportService.importarDemonstracoesContabeis();
            return ResponseEntity.ok("Demonstrativos contábeis baixados com sucesso!");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao baixar demonstrativos: " + e.getMessage());
        }
    }

    @PostMapping("/baixar-operadoras")
    public ResponseEntity<String> baixarDadosOperadoras() {
        dataImportService.importarOperadoras();
        return ResponseEntity.ok("Dados carregados com sucesso via POST");
    }
}
