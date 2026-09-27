package panther_stock_management.backend.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import panther_stock_management.backend.desktop.AppShutdown;

@RestController
public class ShutdownController {

    private final AppShutdown appShutdown;

    public ShutdownController(AppShutdown appShutdown) {
        this.appShutdown = appShutdown;
    }

    @PostMapping("/internal/shutdown")
    public ResponseEntity<Void> shutdown() {
        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            appShutdown.exit();
        });
        shutdownThread.setDaemon(true);
        shutdownThread.start();

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
