package panther_stock_management.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import panther_stock_management.backend.domain.StatusEncomenda;
import panther_stock_management.backend.domain.StatusPedido;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.EncomendaProducaoRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.PedidoReservadoRepository;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private EncomendaProducaoRepository encomendaProducaoRepository;

    @Mock
    private PedidoReservadoRepository pedidoReservadoRepository;

    @InjectMocks
    private EstoqueService estoqueService;

    @Test
    void calculaDisponivelSeguroCombinandoMovimentacoesEncomendasEPedidos() {
        Variacao preto = Variacao.builder().id(1L).nome("Preto").reservaSeguranca(30).build();

        when(movimentacaoRepository.somarQuantidadePorVariacaoETipo(preto, TipoMovimentacao.PRODUCAO_RECEBIDA))
                .thenReturn(120);
        when(movimentacaoRepository.somarQuantidadePorVariacaoETipo(preto, TipoMovimentacao.VENDA)).thenReturn(0);
        when(movimentacaoRepository.somarQuantidadePorVariacaoETipo(preto, TipoMovimentacao.AJUSTE)).thenReturn(0);
        when(encomendaProducaoRepository.somarQuantidadePorVariacaoEStatus(preto, StatusEncomenda.PENDENTE))
                .thenReturn(40);
        when(pedidoReservadoRepository.somarQuantidadePorVariacaoEStatus(preto, StatusPedido.RESERVADO))
                .thenReturn(22);

        int disponivel = estoqueService.disponivelSeguro(preto);

        assertEquals(108, disponivel);
    }

    @Test
    void saldoDeMovimentacoesDescontaVendasESomaAjustesComSinal() {
        Variacao rosa = Variacao.builder().id(2L).nome("Rosa").reservaSeguranca(0).build();

        when(movimentacaoRepository.somarQuantidadePorVariacaoETipo(rosa, TipoMovimentacao.PRODUCAO_RECEBIDA))
                .thenReturn(50);
        when(movimentacaoRepository.somarQuantidadePorVariacaoETipo(rosa, TipoMovimentacao.VENDA)).thenReturn(20);
        when(movimentacaoRepository.somarQuantidadePorVariacaoETipo(rosa, TipoMovimentacao.AJUSTE)).thenReturn(-2);
        when(encomendaProducaoRepository.somarQuantidadePorVariacaoEStatus(rosa, StatusEncomenda.PENDENTE))
                .thenReturn(0);
        when(pedidoReservadoRepository.somarQuantidadePorVariacaoEStatus(rosa, StatusPedido.RESERVADO))
                .thenReturn(0);

        int disponivel = estoqueService.disponivelSeguro(rosa);

        // saldo = 50 (produção) - 20 (venda) + (-2) (ajuste) = 28
        assertEquals(28, disponivel);
    }
}
