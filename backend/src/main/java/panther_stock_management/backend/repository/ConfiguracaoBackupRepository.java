package panther_stock_management.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import panther_stock_management.backend.domain.ConfiguracaoBackup;

public interface ConfiguracaoBackupRepository extends JpaRepository<ConfiguracaoBackup, Long> {
}
