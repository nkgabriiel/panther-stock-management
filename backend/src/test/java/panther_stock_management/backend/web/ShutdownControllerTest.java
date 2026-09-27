package panther_stock_management.backend.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import panther_stock_management.backend.TestcontainersConfiguration;
import panther_stock_management.backend.desktop.AppShutdown;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ShutdownControllerTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALOR = "local-dev-key";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppShutdown appShutdown;

    @Test
    void shutdownSemApiKeyEhRejeitado() throws Exception {
        mockMvc.perform(post("/internal/shutdown")).andExpect(status().isUnauthorized());
    }

    @Test
    void shutdownComApiKeyRetornaAceitoEAcionaOShutdown() throws Exception {
        mockMvc.perform(post("/internal/shutdown").header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isAccepted());

        verify(appShutdown, timeout(1000)).exit();
    }
}
