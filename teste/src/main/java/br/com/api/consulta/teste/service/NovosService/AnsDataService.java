package br.com.api.consulta.teste.service.NovosService;

import br.com.api.consulta.teste.model.novosModel.DemonstracaoContabil;
import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.repository.OperadoraRepository;
import br.com.api.consulta.teste.repository.novosRepository.DemonstracaoContabilRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class AnsDataService {

    @Value("${ans.data.url}")
    private String ansDataUrl;

    private final OperadoraRepository operadoraRepository;
    private final DemonstracaoContabilRepository demonstracaoRepository;

    public AnsDataService(OperadoraRepository operadoraRepository, DemonstracaoContabilRepository demonstracaoRepository) {
        this.operadoraRepository = operadoraRepository;
        this.demonstracaoRepository = demonstracaoRepository;
    }

    @Transactional
    public void importarDadosOperadoras() throws IOException {
        String csvUrl = ansDataUrl + "/operadoras_de_plano_de_saude_ativas/operadoras_ativas.csv";
        List<Operadora> operadoras = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(csvUrl).openStream(), StandardCharsets.ISO_8859_1))) {

            CSVParser csvParser = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                Operadora operadora = mapToOperadora(record);
                if (operadora != null) {
                    operadoras.add(operadora);
                }
            }
        }

        operadoraRepository.saveAll(operadoras);
    }

    private Operadora mapToOperadora(CSVRecord record) {
        try {
            Operadora operadora = new Operadora();
            operadora.setNome(record.get("Razão Social"));
            operadora.setCnpj(record.get("CNPJ").replaceAll("[^0-9]", ""));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            operadora.setDataRegistro(LocalDate.parse(record.get("Data Registro ANS"), formatter));

            operadora.setModalidade(record.get("Modalidade"));
            operadora.setLogradouro(record.get("Logradouro"));
            operadora.setUf(record.get("UF"));

            // Inicializa os campos que são obrigatórios
            operadora.setDespesaSaude(0.0); // Valor padrão
            operadora.setData(LocalDate.now()); // Data atual como padrão

            return operadora;
        } catch (Exception e) {
            System.err.println("Erro ao processar operadora: " + record + " - " + e.getMessage());
            return null;
        }
    }

    @Transactional
    public void importarDemonstracoesContabeis() throws IOException {
        int currentYear = LocalDate.now().getYear();

        for (int year = currentYear - 2; year <= currentYear; year++) {
            String zipUrl = String.format("%s/demonstracoes_contabeis/%d/DemonstracoesContabeis_%d.zip",
                    ansDataUrl, year, year);

            processarDemonstracoesAno(zipUrl, year);
        }
    }

    private void processarDemonstracoesAno(String zipUrl, int year) throws IOException {
        Path tempZip = Files.createTempFile("demonstracoes_" + year, ".zip");
        try (InputStream in = new URL(zipUrl).openStream()) {
            Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
        }

        Path tempDir = Files.createTempDirectory("demonstracoes_" + year);
        try (ZipFile zipFile = new ZipFile(tempZip.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith(".csv") &&
                        entry.getName().contains("Demonstrações Contábeis")) {

                    Path csvPath = tempDir.resolve(entry.getName());
                    try (InputStream entryIn = zipFile.getInputStream(entry)) {
                        Files.copy(entryIn, csvPath, StandardCopyOption.REPLACE_EXISTING);
                        processarCsvDemonstracoes(csvPath, year);
                    }
                }
            }
        } finally {
            Files.deleteIfExists(tempZip);
            deleteDirectory(tempDir);
        }
    }

    private void processarCsvDemonstracoes(Path csvPath, int year) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<DemonstracaoContabil> demonstracoes = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.ISO_8859_1)) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                try {
                    DemonstracaoContabil dc = new DemonstracaoContabil();
                    dc.setCnpj(record.get("CNPJ").replaceAll("[^0-9]", ""));
                    dc.setData(LocalDate.parse(record.get("Data"), dateFormatter));
                    dc.setAnoReferencia(year);

                    dc.setDespesaHospitalar(parseMonetario(
                            record.get("Eventos/Sinistros conhecidos ou avisados de assistência à saúde médico hospitalar")));
                    dc.setDespesaAmbulatorial(parseMonetario(
                            record.get("Eventos/Sinistros conhecidos ou avisados de assistência à saúde ambulatorial")));

                    // Relacionar com Operadora
                    operadoraRepository.findByCnpj(dc.getCnpj())
                            .ifPresent(dc::setOperadora);

                    demonstracoes.add(dc);
                } catch (Exception e) {
                    System.err.println("Erro ao processar registro: " + record + " - " + e.getMessage());
                }
            }
        }

        demonstracaoRepository.saveAll(demonstracoes);
    }

    private BigDecimal parseMonetario(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String cleaned = valor.replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        return new BigDecimal(cleaned);
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("Falha ao deletar arquivo temporário: " + p + " - " + e.getMessage());
                    }
                });
    }
}