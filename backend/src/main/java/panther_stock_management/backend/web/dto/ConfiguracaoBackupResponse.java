package panther_stock_management.backend.web.dto;

import java.time.LocalDateTime;

import panther_stock_management.backend.domain.ConfiguracaoBackup;

public record ConfiguracaoBackupResponse(String pastaLocal, String pastaDrive, LocalDateTime ultimoBackupEm) {

    public static ConfiguracaoBackupResponse de(ConfiguracaoBackup configuracao) {
        return new ConfiguracaoBackupResponse(configuracao.getPastaLocal(), configuracao.getPastaDrive(),
                configuracao.getUltimoBackupEm());
    }
}
