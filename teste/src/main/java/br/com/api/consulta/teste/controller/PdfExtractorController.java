package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.PdfExtractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pdf")
public class PdfExtractorController {
    private final PdfExtractorService pdfExtractorService;

    public PdfExtractorController(PdfExtractorService pdfExtractorService) {
        this.pdfExtractorService = pdfExtractorService;
    }

    @GetMapping("/extrair")
    public ResponseEntity<String> extrairDados() {
        try {
            pdfExtractorService.extrairTextoParaCsv();
            return ResponseEntity.ok("Extração concluída! Arquivo salvo.");
        }catch (Exception e){
            return ResponseEntity.status(500).body("Erro ao extrair dados do PDF: " + e.getMessage());
        }
    }
}

