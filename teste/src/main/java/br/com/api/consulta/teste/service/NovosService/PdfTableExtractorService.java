package br.com.api.consulta.teste.service.NovosService;

import org.springframework.stereotype.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfTableExtractorService {

    private static final Pattern LINHA_TABELA = Pattern.compile(
            "(\\d{4})\\s+(.+?)\\s+(Sim|Não)\\s+(Sim|Não)\\s+(Sim|Não)"
    );

    public File extrairTabelaParaCsv(File pdfFile) throws IOException {
        // 1. Extrair texto do PDF
        String texto = extrairTextoPdf(pdfFile);

        // 2. Processar linhas da tabela
        List<String> linhasTabela = extrairLinhasTabela(texto);

        // 3. Gerar CSV
        return gerarCsv(linhasTabela, pdfFile.getParentFile());
    }

    private String extrairTextoPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private List<String> extrairLinhasTabela(String texto) {
        List<String> linhas = new ArrayList<>();
        linhas.add("CODIGO;DESCRICAO;ODONTOLOGICO;AMBULATORIAL;HOSPITALAR"); // Cabeçalho

        Matcher matcher = LINHA_TABELA.matcher(texto);
        while (matcher.find()) {
            String linha = String.format("%s;%s;%s;%s;%s",
                    matcher.group(1), // Código
                    matcher.group(2).trim(), // Descrição
                    traduzirSimNao(matcher.group(3)), // Odontológico
                    traduzirSimNao(matcher.group(4)), // Ambulatorial
                    traduzirSimNao(matcher.group(5))  // Hospitalar
            );
            linhas.add(linha);
        }

        return linhas;
    }

    private String traduzirSimNao(String valor) {
        return "Sim".equals(valor) ? "Sim" : "Não";
    }

    private File gerarCsv(List<String> linhas, File outputDir) throws IOException {
        File csvFile = new File(outputDir, "procedimentos.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            for (String linha : linhas) {
                writer.write(linha + "\n");
            }
        }
        return csvFile;
    }
}
