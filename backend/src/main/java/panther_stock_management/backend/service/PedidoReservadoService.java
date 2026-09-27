package panther_stock_management.backend.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.PedidoReservado;
import panther_stock_management.backend.domain.StatusPedido;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.PedidoReservadoRepository;

@Service
public class PedidoReservadoService {

    private final PedidoReservadoRepository pedidoReservadoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public PedidoReservadoService(PedidoReservadoRepository pedidoReservadoRepository,
            MovimentacaoRepository movimentacaoRepository) {
        this.pedidoReservadoRepository = pedidoReservadoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public void transicionarPara(PedidoReservado pedido, StatusPedido novoStatus, LocalDate data) {
        StatusPedido atual = pedido.getStatus();

        if (atual == novoStatus) {
            return;
        }

        if (atual != StatusPedido.RESERVADO || novoStatus != StatusPedido.ENVIADO) {
            throw new IllegalStateException(
                    "Transição de status inválida: " + atual + " -> " + novoStatus);
        }

        Movimentacao movimentacao = Movimentacao.builder()
                .variacao(pedido.getVariacao())
                .tipo(TipoMovimentacao.VENDA)
                .quantidade(pedido.getQuantidade())
                .data(data)
                .build();
        movimentacaoRepository.save(movimentacao);

        pedido.setStatus(StatusPedido.ENVIADO);
        pedidoReservadoRepository.save(pedido);
    }
}
