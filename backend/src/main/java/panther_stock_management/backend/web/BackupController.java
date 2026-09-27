package panther_stock_management.backend.web;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import panther_stock_management.backend.backup.BackupExportService;
import panther_stock_management.backend.backup.BackupFileService;
import panther_stock_management.backend.backup.BackupImportService;
import panther_stock_management.backend.backup.ResultadoBackup;
import panther_stock_management.backend.web.dto.ConfiguracaoBackupRequest;
import panther_stock_management.backend.web.dto.ConfiguracaoBackupResponse;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final BackupFileService backupFileService;
    private final BackupExportService backupExportService;
    private final BackupImportService backupImportService;

    public BackupController(BackupFileService backupFileService, BackupExportService backupExportService,
            BackupImportService backupImportService) {
        this.backupFileService = backupFileService;
        this.backupExportService = backupExportService;
        this.backupImportService = backupImportService;
    }

    @GetMapping("/configuracao")
    public ConfiguracaoBackupResponse obterConfiguracao() {
        return ConfiguracaoBackupResponse.de(backupFileService.obterConfiguracao());
    }

    @PutMapping("/configuracao")
    public ConfiguracaoBackupResponse atualizarConfiguracao(@RequestBody ConfiguracaoBackupRequest request) {
        return ConfiguracaoBackupResponse
                .de(backupFileService.atualizarConfiguracao(request.pastaLocal(), request.pastaDrive()));
    }

    @PostMapping("/gerar")
    public ResultadoBackup gerar() {
        return backupFileService.gerarBackup();
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download() {
        byte[] planilha = backupExportService.gerarPlanilha();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("panther-estoque-backup.xlsx").build().toString())
                .body(planilha);
    }

    @PostMapping("/importar")
    public ResponseEntity<Void> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo .xlsx");
        }
        try {
            backupImportService.importar(arquivo.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
        return ResponseEntity.noContent().build();
    }
}
