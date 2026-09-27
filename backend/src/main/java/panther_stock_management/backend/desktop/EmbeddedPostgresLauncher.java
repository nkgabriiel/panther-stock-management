package panther_stock_management.backend.desktop;

import java.io.IOException;
import java.nio.file.Path;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public final class EmbeddedPostgresLauncher {

    private EmbeddedPostgresLauncher() {
    }

    public static void iniciarEConfigurar() {
        try {
            Path dataDir = Path.of(System.getProperty("user.home"), "PantherEstoque", "pgdata");

            EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                    .setDataDirectory(dataDir)
                    .setCleanDataDirectory(false)
                    .start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    postgres.close();
                } catch (IOException ignored) {
                    // encerrando mesmo assim
                }
            }));

            System.setProperty("spring.datasource.url", postgres.getJdbcUrl("postgres", "postgres"));
            System.setProperty("spring.datasource.username", "postgres");
            System.setProperty("spring.datasource.password", "postgres");
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao iniciar o Postgres embutido", e);
        }
    }
}
