package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.CsvImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/procedimentos")
public class ProcedimentoController {
    private final CsvImportService csvImportService;

    public ProcedimentoController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("O arquivo enviado está vazio!");
        }

        try {
            csvImportService.importarCSV(file);
            return ResponseEntity.ok("CSV importado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao importar CSV: " + e.getMessage());
        }
    }
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("A API está funcionando corretamente!");
    }

}
