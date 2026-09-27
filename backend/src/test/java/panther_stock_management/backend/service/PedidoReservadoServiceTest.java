package panther_stock_management.backend.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.PedidoReservado;
import panther_stock_management.backend.domain.StatusPedido;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.PedidoReservadoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PedidoReservadoServiceTest {

    @Mock
    private PedidoReservadoRepository pedidoReservadoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    private PedidoReservadoService service;

    private final LocalDate data = LocalDate.of(2026, 9, 26);

    @Test
    void reservadoParaEnviadoGeraMovimentacaoVendaComQuantidadeCorreta() {
        service = new PedidoReservadoService(pedidoReservadoRepository, movimentacaoRepository);

        Variacao branco = Variacao.builder().id(2L).nome("Branco").build();
        PedidoReservado pedido = PedidoReservado.builder()
                .id(1L)
                .variacao(branco)
                .quantidade(6)
                .status(StatusPedido.RESERVADO)
                .build();

        service.transicionarPara(pedido, StatusPedido.ENVIADO, data);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(captor.capture());

        Movimentacao movimentacao = captor.getValue();
        assertEquals(TipoMovimentacao.VENDA, movimentacao.getTipo());
        assertEquals(6, movimentacao.getQuantidade());
        assertEquals(branco, movimentacao.getVariacao());
        assertEquals(StatusPedido.ENVIADO, pedido.getStatus());
    }

    @Test
    void marcarPedidoJaEnviadoComoEnviadoNovamenteNaoDuplicaMovimentacao() {
        service = new PedidoReservadoService(pedidoReservadoRepository, movimentacaoRepository);

        PedidoReservado pedido = PedidoReservado.builder()
                .id(1L)
                .variacao(Variacao.builder().id(2L).nome("Branco").build())
                .quantidade(6)
                .status(StatusPedido.ENVIADO)
                .build();

        service.transicionarPara(pedido, StatusPedido.ENVIADO, data);

        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void transicaoDeEnviadoParaReservadoEhRejeitada() {
        service = new PedidoReservadoService(pedidoReservadoRepository, movimentacaoRepository);

        PedidoReservado pedido = PedidoReservado.builder()
                .id(1L)
                .variacao(Variacao.builder().id(2L).nome("Branco").build())
                .quantidade(6)
                .status(StatusPedido.ENVIADO)
                .build();

        assertThrows(IllegalStateException.class,
                () -> service.transicionarPara(pedido, StatusPedido.RESERVADO, data));

        verify(movimentacaoRepository, never()).save(any());
    }
}
