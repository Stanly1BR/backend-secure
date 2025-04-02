package br.com.api.consulta.teste.service;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileProcessingService {

    @Value("${ans.rol.url}")
    private String ANS_ROL_URL;

    private static final String DOWNLOAD_DIR = "downloads/";
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "(\\d{4})\\s+(.+?)\\s+(OD|\\S*)\\s+(AMB|\\S*)"
    );

    public File processarRolCompleto() throws IOException {
        // 1. Baixar PDFs
        List<File> pdfs = baixarAnexosANS();

        // 2. Encontrar Anexo I
        File anexoI = pdfs.stream()
                .filter(f -> f.getName().startsWith("Anexo_I"))
                .findFirst()
                .orElseThrow(() -> new IOException("Anexo I não encontrado"));

        // 3. Processar e retornar ZIP com CSV
        return extrairTabelaParaCsv(anexoI);
    }

    public List<File> baixarAnexosANS() throws IOException {
        // Criar diretório se não existir
        Files.createDirectories(Path.of(DOWNLOAD_DIR));

        Document doc = Jsoup.connect(ANS_ROL_URL)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        return doc.select("a[href$=.pdf]").stream()
                .filter(link -> {
                    String href = link.attr("href").toLowerCase();
                    return href.contains("anexo_i") || href.contains("anexo_ii");
                })
                .map(link -> {
                    try {
                        String pdfUrl = link.absUrl("href");
                        String fileName = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);
                        File file = new File(DOWNLOAD_DIR + fileName);

                        System.out.println("Baixando: " + pdfUrl);
                        try (InputStream in = new URL(pdfUrl).openStream()) {
                            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                        return file;
                    } catch (IOException e) {
                        throw new UncheckedIOException("Falha ao baixar arquivo: " + link.absUrl("href"), e);
                    }
                })
                .collect(Collectors.toList());
    }

    public File extrairTabelaParaCsv(File pdfFile) throws IOException {
        String texto = extrairTextoPdf(pdfFile);
        List<String> linhasTabela = parseTableData(texto);
        return generateCsvZip(linhasTabela, pdfFile.getParentFile());
    }

    private String extrairTextoPdf(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private List<String> parseTableData(String texto) {
        List<String> linhas = new ArrayList<>();
        linhas.add("CÓDIGO;DESCRIÇÃO;ODONTOLÓGICO;AMBULATORIAL");

        Matcher matcher = TABLE_PATTERN.matcher(texto);
        while (matcher.find()) {
            linhas.add(formatTableRow(
                    matcher.group(1),  // código
                    matcher.group(2),  // descrição
                    matcher.group(3),  // OD
                    matcher.group(4)   // AMB
            ));
        }
        return linhas;
    }

    private String formatTableRow(String codigo, String descricao, String od, String amb) {
        String odDesc = "OD".equals(od) ? "Odontológico" : "Não Odontológico";
        String ambDesc = "AMB".equals(amb) ? "Ambulatorial" : "Não Ambulatorial";

        return String.join(";",
                codigo,
                descricao.trim(),
                odDesc,
                ambDesc
        );
    }

    private File generateCsvZip(List<String> linhas, File outputDir) throws IOException {
        // Criar CSV temporário
        File csvFile = new File(outputDir, "Teste_IntuitiveCare.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            for (String linha : linhas) {
                writer.write(linha + "\n");
            }
        }

        // Criar ZIP
        File zipFile = new File(outputDir, "Teste_StamlyAlmeidaDoCarmo.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             FileInputStream fis = new FileInputStream(csvFile)) {
            zos.putNextEntry(new ZipEntry(csvFile.getName()));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        } finally {
            Files.deleteIfExists(csvFile.toPath());
        }

        return zipFile;
    }
}