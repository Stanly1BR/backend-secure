package br.com.api.consulta.teste.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class WebScraperService {
    private static final String ANS_URL = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
    private static final String DOWNLOAD_FOLDER = "downloads/";

    public void baixarPdfs() {
        try {
            // Acessa a página da ANS
            Document doc = Jsoup.connect(ANS_URL).get();

            // Busca todos os links que terminam em .pdf
            Elements pdfLinks = doc.select("a[href$=.pdf]");

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
}
