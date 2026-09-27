package panther_stock_management.backend.backup;

import java.time.LocalDateTime;
import java.util.List;

public record ResultadoBackup(List<String> arquivosGravados, int tamanhoBytes, LocalDateTime geradoEm) {
}
