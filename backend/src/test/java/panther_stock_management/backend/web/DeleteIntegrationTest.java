package panther_stock_management.backend.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import panther_stock_management.backend.TestcontainersConfiguration;
import panther_stock_management.backend.domain.ComposicaoKit;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.ComposicaoKitRepository;
import panther_stock_management.backend.repository.VariacaoRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class DeleteIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALOR = "local-dev-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VariacaoRepository variacaoRepository;

    @Autowired
    private ComposicaoKitRepository composicaoKitRepository;

    @Test
    void excluirVariacaoSemHistoricoRemoveEla() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca del\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId
                                + ",\"nome\":\"Cinza\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(delete("/api/variacoes/" + variacaoId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/variacoes/" + variacaoId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirVariacaoComMovimentacaoEhRejeitada() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca del 2\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoId
                                + ",\"nome\":\"Verde\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/movimentacoes")
                .header(API_KEY_HEADER, API_KEY_VALOR)
                .contentType("application/json")
                .content("{\"variacaoId\":" + variacaoId
                        + ",\"tipo\":\"PRODUCAO_RECEBIDA\",\"quantidade\":10,\"data\":\"2026-09-27\"}"));

        mockMvc.perform(delete("/api/variacoes/" + variacaoId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(get("/api/variacoes/" + variacaoId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isOk());
    }

    @Test
    void excluirProdutoSemVariacoesRemoveEle() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Produto vazio del\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(delete("/api/produtos/" + produtoId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/produtos").header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isOk());
    }

    @Test
    void excluirProdutoComVariacoesEhRejeitado() throws Exception {
        Long produtoId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Produto com variacao del\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/variacoes")
                .header(API_KEY_HEADER, API_KEY_VALOR)
                .contentType("application/json")
                .content("{\"produtoId\":" + produtoId + ",\"nome\":\"Azul\",\"reservaSeguranca\":0}"));

        mockMvc.perform(delete("/api/produtos/" + produtoId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void excluirVariacaoUsadaEmComposicaoDeKitEhRejeitada() throws Exception {
        Long produtoUnidadeId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Touca base del\",\"tipo\":\"UNIDADE\"}"))
                .andReturn().getResponse().getContentAsString());
        Long produtoKitId = extrairId(mockMvc.perform(post("/api/produtos")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"nome\":\"Kit del\",\"tipo\":\"KIT\"}"))
                .andReturn().getResponse().getContentAsString());

        Long variacaoBaseId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoUnidadeId
                                + ",\"nome\":\"Amarelo\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());
        Long variacaoKitId = extrairId(mockMvc.perform(post("/api/variacoes")
                        .header(API_KEY_HEADER, API_KEY_VALOR)
                        .contentType("application/json")
                        .content("{\"produtoId\":" + produtoKitId
                                + ",\"nome\":\"1 Amarelo\",\"reservaSeguranca\":0}"))
                .andReturn().getResponse().getContentAsString());

        Variacao variacaoBase = variacaoRepository.findById(variacaoBaseId).orElseThrow();
        Variacao variacaoKit = variacaoRepository.findById(variacaoKitId).orElseThrow();
        composicaoKitRepository.save(ComposicaoKit.builder()
                .kitVariacao(variacaoKit).variacaoBase(variacaoBase).quantidade(1).build());

        mockMvc.perform(delete("/api/variacoes/" + variacaoBaseId).header(API_KEY_HEADER, API_KEY_VALOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").exists());
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
