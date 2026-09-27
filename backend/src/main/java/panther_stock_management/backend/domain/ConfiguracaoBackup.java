package panther_stock_management.backend.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoBackup {

    @Id
    private Long id;

    private String pastaLocal;

    private String pastaDrive;

    private LocalDateTime ultimoBackupEm;
}
