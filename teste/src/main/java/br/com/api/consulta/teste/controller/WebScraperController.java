package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.CsvImportService;
import br.com.api.consulta.teste.service.PdfExtractorService;
import br.com.api.consulta.teste.service.WebScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scraping")
public class WebScraperController {
    private final WebScraperService webScraperService;
    private final PdfExtractorService pdfExtractorService;
    private final CsvImportService csvImportService;

    public WebScraperController(WebScraperService webScraperService, PdfExtractorService pdfExtractorService, CsvImportService csvImportService) {
        this.webScraperService = webScraperService;
        this.pdfExtractorService = pdfExtractorService;
        this.csvImportService = csvImportService;
    }

    @GetMapping("/baixar-pdfs")
    public ResponseEntity<String> processarDados() {
        try{
        webScraperService.baixarPdfs();
        pdfExtractorService.extrairTextoParaCsv();
        return ResponseEntity.ok("Download de PDFs concluído!");
    }catch (Exception e){
        return  ResponseEntity.status(500).body("Erro ao extrair dados do PDF: " + e.getMessage());
        }
    }
}

