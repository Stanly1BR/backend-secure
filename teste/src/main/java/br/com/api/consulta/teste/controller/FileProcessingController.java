package br.com.api.consulta.teste.controller;

import br.com.api.consulta.teste.service.DataImportService;
import br.com.api.consulta.teste.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/processamento")
@RequiredArgsConstructor
public class FileProcessingController {

    private final FileProcessingService fileProcessingService;
    private final DataImportService dataImportService;

    // ========== ENDPOINTS PARA DOWNLOAD E PROCESSAMENTO AUTOMÁTICO ==========

    /**
     * Executa o fluxo completo: download dos PDFs, extração da tabela e geração do CSV
     */
    @GetMapping("/processar-rol-completo")
    public ResponseEntity<String> processarRolCompleto() {
        try {
            File resultado = fileProcessingService.processarRolCompleto();
            return ResponseEntity.ok("Arquivo gerado com sucesso: " + resultado.getName());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar Rol: " + e.getMessage());
        }
    }

    /**
     * Apenas baixa os anexos da ANS
     */
    @GetMapping("/baixar-anexos")
    public ResponseEntity<String> baixarAnexos() {
        try {
            List<File> arquivos = fileProcessingService.baixarAnexosANS();
            return ResponseEntity.ok(
                    "Download concluído. Arquivos: " +
                            arquivos.stream().map(File::getName).collect(Collectors.joining(", "))
            );
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao baixar anexos: " + e.getMessage());
        }
    }

    // ========== ENDPOINTS PARA PROCESSAMENTO MANUAL ==========

    /**
     * Processa um arquivo PDF enviado manualmente
     */
    @PostMapping("/extrair-tabela")
    public ResponseEntity<String> extrairTabelaDePdf(@RequestParam("file") MultipartFile file) {
        try {
            // Salva o arquivo temporariamente
            File pdfFile = File.createTempFile("anexo_", ".pdf");
            file.transferTo(pdfFile);

            // Processa e retorna o resultado
            File resultado = fileProcessingService.extrairTabelaParaCsv(pdfFile);
            return ResponseEntity.ok("CSV gerado com sucesso: " + resultado.getName());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar PDF: " + e.getMessage());
        }
    }

    /**
     * Importa procedimentos de um CSV
     */
    @PostMapping("/importar-procedimentos")
    public ResponseEntity<String> importarProcedimentos(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo vazio");
        }

        try {
            dataImportService.importarProcedimentos(file.getInputStream());
            return ResponseEntity.ok("Procedimentos importados com sucesso");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao importar procedimentos: " + e.getMessage());
        }
    }

    // ========== ENDPOINT DE VERIFICAÇÃO ==========

    @GetMapping("/status")
    public ResponseEntity<String> verificarStatus() {
        return ResponseEntity.ok("Serviço de processamento ativo");
    }
}
