package panther_stock_management.backend.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import panther_stock_management.backend.domain.ConfiguracaoBackup;
import panther_stock_management.backend.repository.ConfiguracaoBackupRepository;

@Service
public class BackupFileService {

    private static final DateTimeFormatter NOME_ARQUIVO_FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private final ConfiguracaoBackupRepository configuracaoBackupRepository;
    private final BackupExportService backupExportService;
    private final String pastaLocalPadrao;

    public BackupFileService(ConfiguracaoBackupRepository configuracaoBackupRepository,
            BackupExportService backupExportService,
            @Value("${app.backup.local-dir}") String pastaLocalPadrao) {
        this.configuracaoBackupRepository = configuracaoBackupRepository;
        this.backupExportService = backupExportService;
        this.pastaLocalPadrao = pastaLocalPadrao;
    }

    public ConfiguracaoBackup obterConfiguracao() {
        return configuracaoBackupRepository.findById(1L)
                .orElseGet(() -> configuracaoBackupRepository.save(ConfiguracaoBackup.builder().id(1L).build()));
    }

    public ConfiguracaoBackup atualizarConfiguracao(String pastaLocal, String pastaDrive) {
        ConfiguracaoBackup configuracao = obterConfiguracao();
        configuracao.setPastaLocal(pastaLocal);
        configuracao.setPastaDrive(pastaDrive);
        return configuracaoBackupRepository.save(configuracao);
    }

    public ResultadoBackup gerarBackup() {
        ConfiguracaoBackup configuracao = obterConfiguracao();
        byte[] planilha = backupExportService.gerarPlanilha();
        String nomeArquivo = "panther-estoque-backup-" + LocalDateTime.now().format(NOME_ARQUIVO_FORMATO) + ".xlsx";

        String pastaLocal = temValor(configuracao.getPastaLocal()) ? configuracao.getPastaLocal() : pastaLocalPadrao;

        List<String> arquivosGravados = new ArrayList<>();
        arquivosGravados.add(gravarArquivo(pastaLocal, nomeArquivo, planilha));
        if (temValor(configuracao.getPastaDrive())) {
            arquivosGravados.add(gravarArquivo(configuracao.getPastaDrive(), nomeArquivo, planilha));
        }

        configuracao.setUltimoBackupEm(LocalDateTime.now());
        configuracaoBackupRepository.save(configuracao);

        return new ResultadoBackup(arquivosGravados, planilha.length, configuracao.getUltimoBackupEm());
    }

    private boolean temValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String gravarArquivo(String pasta, String nomeArquivo, byte[] conteudo) {
        try {
            Path diretorio = Path.of(pasta);
            Files.createDirectories(diretorio);
            Path arquivo = diretorio.resolve(nomeArquivo);
            Files.write(arquivo, conteudo);
            return arquivo.toString();
        } catch (IOException | java.nio.file.InvalidPathException e) {
            throw new BackupIOException("Não foi possível salvar o backup em \"" + pasta + "\"", e);
        }
    }
}
