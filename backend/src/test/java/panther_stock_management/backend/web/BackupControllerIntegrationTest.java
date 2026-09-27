package panther_stock_management.backend.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import panther_stock_management.backend.TestcontainersConfiguration;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class BackupControllerIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALOR = "local-dev-key";

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    Path pastaLocal;

    @TempDir
    Path pastaDrive;

    @Test
    void configurarPastasEGerarBackupGravaArquivoNasDuasPastas() throws Exception {
        String corpo = "{\"pastaLocal\":" + paraJson(pastaLocal) + ",\"pastaDrive\":" + paraJson(pastaDrive) + "}";

        mockMvc.perform(put("/api/backup/configuracao")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/backup/gerar").header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arquivosGravados", hasSize(2)));

        try (Stream<Path> localFiles = Files.list(pastaLocal)) {
            assertEquals(1, localFiles.count());
        }
        try (Stream<Path> driveFiles = Files.list(pastaDrive)) {
            assertEquals(1, driveFiles.count());
        }
    }

    @Test
    void downloadRetornaPlanilhaValida() throws Exception {
        mockMvc.perform(get("/api/backup/download").header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsByteArray().length > 0));
    }

    @Test
    void importarPlanilhaGeradaPeloProprioSistemaFunciona() throws Exception {
        byte[] planilha = mockMvc.perform(get("/api/backup/download").header(API_KEY_HEADER, API_KEY_VALOR))
                .andReturn().getResponse().getContentAsByteArray();

        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "backup.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", planilha);

        mockMvc.perform(multipart("/api/backup/importar").file(arquivo).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isNoContent());
    }

    @Test
    void importarSemArquivoRetorna400() throws Exception {
        MockMultipartFile arquivoVazio = new MockMultipartFile("arquivo", "vazio.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        mockMvc.perform(multipart("/api/backup/importar").file(arquivoVazio).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isBadRequest());
    }

    private String paraJson(Path path) {
        String escapado = path.toString().replace("\\", "\\\\");
        return "\"" + escapado + "\"";
    }
}
