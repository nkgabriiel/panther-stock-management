package panther_stock_management.backend.desktop;

import org.springframework.stereotype.Component;

@Component
public class SystemExitAppShutdown implements AppShutdown {

    @Override
    public void exit() {
        System.exit(0);
    }
}
