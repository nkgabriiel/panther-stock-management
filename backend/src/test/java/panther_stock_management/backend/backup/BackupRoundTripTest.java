package panther_stock_management.backend.backup;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import panther_stock_management.backend.TestcontainersConfiguration;
import panther_stock_management.backend.domain.ComposicaoKit;
import panther_stock_management.backend.domain.EncomendaProducao;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.PedidoReservado;
import panther_stock_management.backend.domain.Produto;
import panther_stock_management.backend.domain.StatusEncomenda;
import panther_stock_management.backend.domain.StatusPedido;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.TipoProduto;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.ComposicaoKitRepository;
import panther_stock_management.backend.repository.EncomendaProducaoRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.PedidoReservadoRepository;
import panther_stock_management.backend.repository.ProdutoRepository;
import panther_stock_management.backend.repository.VariacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BackupRoundTripTest {

    @Autowired
    private BackupExportService exportService;

    @Autowired
    private BackupImportService importService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VariacaoRepository variacaoRepository;

    @Autowired
    private ComposicaoKitRepository composicaoKitRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private EncomendaProducaoRepository encomendaProducaoRepository;

    @Autowired
    private PedidoReservadoRepository pedidoReservadoRepository;

    @Test
    @Transactional
    void exportarEImportarReproduzOMesmoEstadoDeDados() {
        Produto produtoUnidade = produtoRepository.save(
                Produto.builder().nome("Touca unidade RT").tipo(TipoProduto.UNIDADE).build());
        Produto produtoKit = produtoRepository
                .save(Produto.builder().nome("Kit RT").tipo(TipoProduto.KIT).build());

        Variacao preto = variacaoRepository.save(Variacao.builder()
                .produto(produtoUnidade).nome("Preto RT").reservaSeguranca(30).estoqueAnunciado(90).build());
        Variacao branco = variacaoRepository.save(Variacao.builder()
                .produto(produtoUnidade).nome("Branco RT").reservaSeguranca(15).estoqueAnunciado(40).build());
        Variacao kitVariacao = variacaoRepository.save(Variacao.builder()
                .produto(produtoKit).nome("1 Preto + 1 Branco RT").reservaSeguranca(1).estoqueAnunciado(2).build());

        composicaoKitRepository.save(ComposicaoKit.builder()
                .kitVariacao(kitVariacao).variacaoBase(preto).quantidade(1).build());
        composicaoKitRepository.save(ComposicaoKit.builder()
                .kitVariacao(kitVariacao).variacaoBase(branco).quantidade(1).build());

        movimentacaoRepository.save(Movimentacao.builder()
                .variacao(preto).data(LocalDate.of(2026, 9, 17)).tipo(TipoMovimentacao.PRODUCAO_RECEBIDA)
                .quantidade(120).build());
        movimentacaoRepository.save(Movimentacao.builder()
                .variacao(preto).data(LocalDate.of(2026, 9, 18)).tipo(TipoMovimentacao.VENDA)
                .quantidade(10).build());

        encomendaProducaoRepository.save(EncomendaProducao.builder()
                .variacao(branco).quantidade(20).dataPrevista(LocalDate.of(2026, 10, 1))
                .status(StatusEncomenda.PENDENTE).build());

        pedidoReservadoRepository.save(PedidoReservado.builder()
                .variacao(preto).quantidade(5).dataVenda(LocalDate.of(2026, 9, 19))
                .status(StatusPedido.RESERVADO).build());

        byte[] planilha = exportService.gerarPlanilha();
        assertTrue(planilha.length > 0);

        importService.importar(planilha);

        assertEquals(2, produtoRepository.count());
        assertEquals(3, variacaoRepository.count());
        assertEquals(2, composicaoKitRepository.count());
        assertEquals(2, movimentacaoRepository.count());
        assertEquals(1, encomendaProducaoRepository.count());
        assertEquals(1, pedidoReservadoRepository.count());

        Variacao pretoRestaurado = variacaoRepository.findById(preto.getId()).orElseThrow();
        assertEquals("Preto RT", pretoRestaurado.getNome());
        assertEquals(30, pretoRestaurado.getReservaSeguranca());
        assertEquals(90, pretoRestaurado.getEstoqueAnunciado());

        // uma nova variação criada após a importação deve receber um id livre,
        // sem colidir com os ids restaurados (sequência do Postgres precisa estar correta)
        Variacao novaVariacao = variacaoRepository.save(Variacao.builder()
                .produto(produtoUnidade).nome("Rosa pós-import").reservaSeguranca(0).estoqueAnunciado(0).build());
        assertTrue(novaVariacao.getId() > kitVariacao.getId());
    }
}
