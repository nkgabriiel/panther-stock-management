package panther_stock_management.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

import panther_stock_management.backend.desktop.EmbeddedPostgresLauncher;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BackendApplication {

	public static void main(String[] args) {
		if ("true".equals(System.getenv("APP_EMBEDDED_POSTGRES"))) {
			EmbeddedPostgresLauncher.iniciarEConfigurar();
		}
		SpringApplication.run(BackendApplication.class, args);
	}

}
