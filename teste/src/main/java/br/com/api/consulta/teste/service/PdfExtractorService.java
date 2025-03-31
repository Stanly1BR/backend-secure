package br.com.api.consulta.teste.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class PdfExtractorService {
    private static final String PDF_FILE = "downloads/anexo_I.pdf";  // Mantido para compatibilidade
    private static final String CSV_FILE = "downloads/dados_extraidos.csv"; // Mantido

    public void extrairTextoParaCsv() {
        try {

            // Busca o arquivo que começa com "Anexo_I" na pasta downloads
            File downloadDir = new File("downloads/");
            File[] pdfFiles = downloadDir.listFiles((dir, name) ->
                    name.startsWith("Anexo_I") && name.endsWith(".pdf"));

            if (pdfFiles == null || pdfFiles.length == 0) {
                System.out.println("Nenhum PDF encontrado em: downloads/");
                return;
            }

            File pdfFile = pdfFiles[0]; // Pega o primeiro arquivo encontrado
            System.out.println("Processando arquivo: " + pdfFile.getName());

            // Carrega o PDF
            PDDocument document = PDDocument.load(pdfFile);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String textoExtraido = pdfStripper.getText(document);
            document.close();

            // Processa os dados extraídos
            List<String> linhas = Arrays.asList(textoExtraido.split("\n"));
            List<String> dadosFiltrados = linhas.stream()
                    .filter(linha -> linha.contains("CODIGO") || linha.matches(".*\\d{3,}.*"))
                    .collect(Collectors.toList());

            // Salva os dados em CSV
            salvarComoCsv(dadosFiltrados);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarComoCsv(List<String> dados) {
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            for (String linha : dados) {
                writer.append(linha.replace(" ", ";")).append("\n");
            }
            System.out.println("Dados salvos em: " + CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}