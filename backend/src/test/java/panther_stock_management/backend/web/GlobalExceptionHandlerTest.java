package panther_stock_management.backend.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import panther_stock_management.backend.TestcontainersConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALOR = "local-dev-key";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cadastroDeProdutoSemNomeRetorna400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"tipo\":\"UNIDADE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    void cadastroDeVariacaoComProdutoInexistenteRetorna404ComMensagem() throws Exception {
        mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":999999,\"nome\":\"Preto\",\"reservaSeguranca\":10}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void lancamentoDeMovimentacaoComVariacaoInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/movimentacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"variacaoId\":999999,\"tipo\":\"VENDA\",\"quantidade\":1,"
                                + "\"data\":\"2026-09-26\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }
}
