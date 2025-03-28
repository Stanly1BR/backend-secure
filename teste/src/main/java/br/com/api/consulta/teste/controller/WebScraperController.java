package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.WebScraperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scraping")
public class WebScraperController {
    private final WebScraperService webScraperService;

    public WebScraperController(WebScraperService webScraperService) {
        this.webScraperService = webScraperService;
    }

    @GetMapping("/baixar-pdfs")
    public String iniciarDownload() {
        webScraperService.baixarPdfs();
        return "Download de PDFs concluído!";
    }
}

