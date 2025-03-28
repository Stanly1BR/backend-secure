package br.com.api.consulta.teste.service;

import br.com.api.consulta.teste.model.Procedimento;
import br.com.api.consulta.teste.repository.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvImportService {
    private final ProcedimentoRepository procedimentoRepository;

    public CsvImportService(ProcedimentoRepository procedimentoRepository) {
        this.procedimentoRepository = procedimentoRepository;
    }

    public void importarCSV(MultipartFile file) {
        List<Procedimento> procedimentos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Pular o cabeçalho
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";", -1); // Ajuste conforme o delimitador do CSV

                if (fields.length >= 4) {
                    Procedimento procedimento = new Procedimento();
                    procedimento.setCodigo(fields[0].trim());
                    procedimento.setDescricao(fields[1].trim());
                    procedimento.setAmbulatorial(fields[2].trim());
                    procedimento.setHospitalar(fields[3].trim());
                    procedimentos.add(procedimento);
                    if(!procedimento.getCodigo().isEmpty()){
                        procedimentos.add(procedimento);
                    }
                }
            }
            if (!procedimentos.isEmpty()) {
                procedimentoRepository.saveAll(procedimentos);
            }else {
                throw new IllegalArgumentException("Nenhum procedimento válido encontrado no arquivo.");
            }
        }catch (Exception e) {
            throw new RuntimeException("Erro ao importar CSV: " + e.getMessage());
        }
    }
}
