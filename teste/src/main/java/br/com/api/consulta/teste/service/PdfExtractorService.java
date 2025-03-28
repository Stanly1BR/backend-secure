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
    private static final String PDF_FILE = "downloads/anexo_I.pdf";  // Caminho do PDF baixado
    private static final String CSV_FILE = "downloads/dados_extraidos.csv"; // Caminho do CSV

    public void extrairTextoParaCsv() {
        try {
            File file = new File(PDF_FILE);
            if (!file.exists()) {
                System.out.println("Arquivo PDF não encontrado: " + PDF_FILE);
                return;
            }

            // Carrega o PDF
            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String textoExtraido = pdfStripper.getText(document);
            document.close();

            // Processa os dados extraídos
            List<String> linhas = Arrays.asList(textoExtraido.split("\n"));
            List<String> dadosFiltrados = linhas.stream()
                    .filter(linha -> linha.contains("CODIGO") || linha.matches(".*\\d{3,}.*")) // Filtra linhas da tabela
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
                writer.append(linha.replace(" ", ";")).append("\n");  // Substitui espaços por ponto e vírgula
            }
            System.out.println("Dados salvos em: " + CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
