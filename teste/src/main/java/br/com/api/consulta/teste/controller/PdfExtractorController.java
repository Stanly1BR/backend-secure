package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.PdfExtractorService;
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
    public String extrairDados() {
        pdfExtractorService.extrairTextoParaCsv();
        return "Extração concluída! Arquivo salvo.";
    }
}

