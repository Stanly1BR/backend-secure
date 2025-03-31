package br.com.api.consulta.teste.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WebScraperService {
    private static final String ANS_URL = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
    private static final String DOWNLOAD_FOLDER = System.getProperty("user.dir") + "/downloads/";

    public void baixarPdfs() {
        try {
            // Acessa a página da ANS
            Document doc = Jsoup.connect(ANS_URL).get();

            // Busca todos os links que terminam em .pdf
            Elements pdfLinks = doc.select("a[href$=.pdf]").stream().filter(link -> link.attr("href").contains("Anexo_I") || link.attr("href").contains("Anexo_II")).collect(Collectors.toCollection(Elements::new));

            // Criar a pasta se não existir
            Files.createDirectories(Path.of(DOWNLOAD_FOLDER));

            // Percorre os links encontrados
            for (Element link : pdfLinks) {
                String pdfUrl = link.absUrl("href");
                String fileName = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);

                System.out.println("Baixando: " + pdfUrl);
                baixarArquivo(pdfUrl, DOWNLOAD_FOLDER + fileName);
            }

            System.out.println("Download concluído!");
            compactarAnexos();
            System.out.println("Download e compactação concluídos!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void baixarArquivo(String fileUrl, String destino) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, Path.of(destino), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Arquivo salvo em: " + destino);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compactarAnexos() {
        File downloadDir = new File(DOWNLOAD_FOLDER);
        File[] arquivosParaZipar = downloadDir.listFiles((dir, name) ->
                name.startsWith("Anexo_I") || name.startsWith("Anexo_II"));

        String zipFile = DOWNLOAD_FOLDER + "Anexos_ANS.zip";

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File fileToZip : arquivosParaZipar) {
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                }
            }
            System.out.println("Arquivos compactados com sucesso em: " + zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao compactar arquivos: " + e.getMessage());
        }
    }
}
