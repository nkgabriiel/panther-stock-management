package panther_stock_management.backend.web;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import panther_stock_management.backend.TestcontainersConfiguration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALOR = "local-dev-key";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requisicaoSemApiKeyEhRejeitada() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requisicaoComApiKeyInvalidaEhRejeitada() throws Exception {
        mockMvc.perform(get("/api/produtos").header(API_KEY_HEADER, "chave-errada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroDeProdutoRetorna201ComOsDadosCorretos() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca unidade\",\"tipo\":\"UNIDADE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Touca unidade"))
                .andExpect(jsonPath("$.tipo").value("UNIDADE"));
    }

    @Test
    void cadastroDeVariacaoRetorna201ComOsDadosCorretos() throws Exception {
        String produtoJson = mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca unidade\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString();
        Long produtoId = extrairId(produtoJson);

        mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId
                                + ",\"nome\":\"Preto\",\"reservaSeguranca\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Preto"))
                .andExpect(jsonPath("$.reservaSeguranca").value(30))
                .andExpect(jsonPath("$.disponivelSeguro").value(-30))
                .andExpect(jsonPath("$.estoqueAnunciado").value(0))
                .andExpect(jsonPath("$.folga").value(-30))
                .andExpect(jsonPath("$.statusAnuncio").value("REDUZIR"));
    }

    @Test
    void atualizarEstoqueAnunciadoRecalculaFolgaEStatus() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca unidade\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId
                                + ",\"nome\":\"Preto anuncio\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/movimentacoes")
                .header(API_KEY_HEADER, API_KEY_VALOR)
                .contentType("application/json")
                .content("{\"variacaoId\":" + variacaoId
                        + ",\"tipo\":\"PRODUCAO_RECEBIDA\",\"quantidade\":108,\"data\":\"2026-09-17\"}"));

        mockMvc.perform(patch("/api/variacoes/" + variacaoId + "/estoque-anunciado")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"estoqueAnunciado\":90}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueAnunciado").value(90))
                .andExpect(jsonPath("$.disponivelSeguro").value(108))
                .andExpect(jsonPath("$.folga").value(18))
                .andExpect(jsonPath("$.statusAnuncio").value("PODE_ANUNCIAR"));
    }

    @Test
    void lancamentoDeMovimentacaoAtualizaOEstoqueERetornaOSaldoAtualizado() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca unidade\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId + ",\"nome\":\"Branco\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/movimentacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"variacaoId\":" + variacaoId
                                + ",\"tipo\":\"PRODUCAO_RECEBIDA\",\"quantidade\":45,\"data\":\"2026-09-17\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoAtualizado").value(45));

        mockMvc.perform(post("/api/movimentacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"variacaoId\":" + variacaoId
                                + ",\"tipo\":\"VENDA\",\"quantidade\":6,\"data\":\"2026-09-17\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoAtualizado").value(39));
    }

    @Test
    void listagemDeHistoricoRespeitaOsFiltrosDePeriodoProdutoECor() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca unidade filtro\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId + ",\"nome\":\"Rosa filtro\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/movimentacoes")
                .header(API_KEY_HEADER, API_KEY_VALOR)
                .contentType("application/json")
                .content("{\"variacaoId\":" + variacaoId
                        + ",\"tipo\":\"PRODUCAO_RECEBIDA\",\"quantidade\":10,\"data\":\"2026-09-10\"}"));
        mockMvc.perform(post("/api/movimentacoes")
                .header(API_KEY_HEADER, API_KEY_VALOR)
                .contentType("application/json")
                .content("{\"variacaoId\":" + variacaoId
                        + ",\"tipo\":\"PRODUCAO_RECEBIDA\",\"quantidade\":5,\"data\":\"2026-09-20\"}"));

        mockMvc.perform(get("/api/movimentacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .param("produtoId", String.valueOf(produtoId))
                        .param("dataInicio", "2026-09-01")
                        .param("dataFim", "2026-09-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantidade").value(10));
    }

    @Test
    void listagemDeHistoricoSemNenhumFiltroRetornaTudo() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca unidade sem filtro\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId
                                + ",\"nome\":\"Preto sem filtro\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/movimentacoes")
                .header(API_KEY_HEADER, API_KEY_VALOR)
                .contentType("application/json")
                .content("{\"variacaoId\":" + variacaoId
                        + ",\"tipo\":\"PRODUCAO_RECEBIDA\",\"quantidade\":7,\"data\":\"2026-09-17\"}"));

        mockMvc.perform(get("/api/movimentacoes").header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isOk());
    }

    private Long extrairId(String json) {
        int idx = json.indexOf("\"id\":");
        String rest = json.substring(idx + 5);
        StringBuilder digits = new StringBuilder();
        for (char c : rest.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (!digits.isEmpty()) {
                break;
            }
        }
        return Long.parseLong(digits.toString());
    }
}
