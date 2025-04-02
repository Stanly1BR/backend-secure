package br.com.api.consulta.teste.service;

import br.com.api.consulta.teste.model.Operadora;
import br.com.api.consulta.teste.model.Procedimento;
import br.com.api.consulta.teste.model.DemonstracaoContabil;
import br.com.api.consulta.teste.repository.OperadoraRepository;
import br.com.api.consulta.teste.repository.ProcedimentoRepository;
import br.com.api.consulta.teste.repository.DemonstracaoContabilRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@RequiredArgsConstructor
public class DataImportService {

    @Value("${ans.data.url}")
    private String ansDataUrl;

    private final OperadoraRepository operadoraRepository;
    private final DemonstracaoContabilRepository demonstracaoRepository;
    private final ProcedimentoRepository procedimentoRepository;

    // ========== IMPORTAR DADOS DA ANS ==========

    @Transactional
    public void importarDadosCompletosANS() throws IOException {
        importarOperadoras();
        importarDemonstracoesContabeis();
    }

    @Transactional
    public void importarOperadoras() {
        try {
            // 1. Limpa a tabela antes de importar (evita duplicatas)
            operadoraRepository.deleteAllInBatch();

            // 2. Importa os dados
            List<Operadora> operadoras = carregarOperadoras();
            operadoraRepository.saveAll(operadoras);

        } catch (Exception e) {
            System.err.println("Erro na importação: " + e.getMessage());
            criarOperadorasPadrao();
        }
    }

    private List<Operadora> carregarOperadoras() {
        try (InputStream is = getClass().getResourceAsStream("/backup/operadoras_fallback.csv")) {
            if (is != null) {
                return parseOperadorasCsv(is);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private void criarOperadorasPadrao() {
        try {
            List<Operadora> padroes = List.of(
                    criarOperadora("00000000000191", "OPERADORA PADRÃO A", "MEDICINA", "SP"),
                    criarOperadora("00000000000272", "OPERADORA PADRÃO B", "ODONTOLOGIA", "RJ")
            );
            operadoraRepository.saveAll(padroes);
        } catch (Exception e) {
            System.err.println("Erro crítico no fallback: " + e.getMessage());
            // Se até o fallback falhar, pelo menos não quebra a transação
        }
    }

    private Operadora criarOperadora(String cnpj, String nome, String modalidade, String uf) {
        Operadora op = new Operadora();
        op.setCnpj(cnpj.replaceAll("[^0-9]", ""));
        op.setNome(nome);
        op.setModalidade(modalidade);
        op.setUf(uf);
        op.setDataRegistro(LocalDate.now());
        return op;
    }

    private void criarBackupBasico(File destino) throws IOException {
        String conteudoPadrao =
                "CNPJ;Razão Social;Data Registro ANS;Modalidade;UF;Logradouro\n" +
                        "00000000000191;OPERADORA PADRÃO 1;01/01/2023;MEDICINA;SP;Rua Exemplo, 123\n" +
                        "00000000000272;OPERADORA PADRÃO 2;01/01/2023;ODONTOLOGIA;RJ;Av. Teste, 456";

        Files.write(destino.toPath(), conteudoPadrao.getBytes(StandardCharsets.ISO_8859_1));
        System.out.println("Arquivo backup criado: " + destino.getAbsolutePath());
    }

    // Método compartilhado de parsing
    private List<Operadora> parseOperadorasCsv(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        CSVParser parser = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .parse(reader);

        return StreamSupport.stream(parser.spliterator(), false)
                .map(this::mapToOperadora)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    // ========== IMPORTAR PROCEDIMENTOS ==========

    @Transactional
    public void importarProcedimentos(MultipartFile file) throws IOException {
        List<Procedimento> procedimentos = parseProcedimentosFromCSV(file.getInputStream());

        if (procedimentos.isEmpty()) {
            throw new IllegalArgumentException("Nenhum procedimento válido encontrado no arquivo.");
        }

        procedimentoRepository.saveAll(procedimentos);
    }

    @Transactional
    public void importarProcedimentos(InputStream inputStream) throws IOException {
        List<Procedimento> procedimentos = parseProcedimentosFromCSV(inputStream);
        procedimentoRepository.saveAll(procedimentos);
    }

    // ========== MÉTODOS PRIVADOS COMPARTILHADOS ==========

    private Operadora mapToOperadora(CSVRecord record) {
        try {
            Operadora operadora = new Operadora();
            operadora.setNome(record.get("Razão Social"));

            // Limpa o CNPJ garantindo que tenha exatamente 14 dígitos
            String cnpj = record.get("CNPJ").replaceAll("[^0-9]", "");
            if (cnpj.length() != 14) {
                cnpj = cnpj.substring(0, Math.min(cnpj.length(), 14)); // Garante no máximo 14 caracteres
                if (cnpj.length() < 14) {
                    cnpj = String.format("%14s", cnpj).replace(' ', '0'); // Completa com zeros à esquerda
                }
            }
            operadora.setCnpj(cnpj);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            operadora.setDataRegistro(LocalDate.parse(record.get("Data Registro ANS"), formatter));

            operadora.setModalidade(record.get("Modalidade"));
            operadora.setLogradouro(record.get("Logradouro"));
            operadora.setUf(record.get("UF"));

            operadora.setDespesaSaude(BigDecimal.ZERO);
            return operadora;
        } catch (Exception e) {
            throw new DataImportException("Erro ao mapear operadora: " + record, e);
        }
    }

    private List<Procedimento> parseProcedimentosFromCSV(InputStream inputStream) throws IOException {
        List<Procedimento> procedimentos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            br.readLine(); // Pular cabeçalho

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";", -1);

                if (fields.length >= 4 && !fields[0].trim().isEmpty()) {
                    Procedimento procedimento = new Procedimento();
                    procedimento.setCodigo(fields[0].trim());
                    procedimento.setDescricao(fields[1].trim());
                    procedimento.setAmbulatorial(fields[2].trim());
                    procedimento.setHospitalar(fields[3].trim());
                    procedimentos.add(procedimento);
                }
            }
        }

        return procedimentos;
    }

    // ========== MÉTODOS PRIVADOS PARA DEMONSTRAÇÕES CONTÁBEIS ==========

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

                    operadoraRepository.findByCnpj(dc.getCnpj()).ifPresent(dc::setOperadora);
                    demonstracoes.add(dc);
                } catch (Exception e) {
                    throw new DataImportException("Erro ao processar registro: " + record, e);
                }
            }
        }

        demonstracaoRepository.saveAll(demonstracoes);
    }

    // ========== MÉTODOS UTILITÁRIOS ==========

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
                        throw new DataImportException("Falha ao deletar arquivo temporário: " + p, e);
                    }
                });
    }

    // ========== EXCEÇÃO PERSONALIZADA ==========

    public static class DataImportException extends RuntimeException {
        public DataImportException(String message) {
            super(message);
        }

        public DataImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}