package panther_stock_management.backend.service;

import org.springframework.stereotype.Service;

import panther_stock_management.backend.domain.StatusEncomenda;
import panther_stock_management.backend.domain.StatusPedido;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.EncomendaProducaoRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.PedidoReservadoRepository;

@Service
public class EstoqueService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final EncomendaProducaoRepository encomendaProducaoRepository;
    private final PedidoReservadoRepository pedidoReservadoRepository;

    public EstoqueService(MovimentacaoRepository movimentacaoRepository,
            EncomendaProducaoRepository encomendaProducaoRepository,
            PedidoReservadoRepository pedidoReservadoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.encomendaProducaoRepository = encomendaProducaoRepository;
        this.pedidoReservadoRepository = pedidoReservadoRepository;
    }

    public int saldoMovimentacoes(Variacao variacao) {
        int producaoRecebida = movimentacaoRepository.somarQuantidadePorVariacaoETipo(variacao,
                TipoMovimentacao.PRODUCAO_RECEBIDA);
        int venda = movimentacaoRepository.somarQuantidadePorVariacaoETipo(variacao, TipoMovimentacao.VENDA);
        int ajuste = movimentacaoRepository.somarQuantidadePorVariacaoETipo(variacao, TipoMovimentacao.AJUSTE);
        return producaoRecebida - venda + ajuste;
    }

    public int producaoGarantidaPendente(Variacao variacao) {
        return encomendaProducaoRepository.somarQuantidadePorVariacaoEStatus(variacao, StatusEncomenda.PENDENTE);
    }

    public int pedidosReservados(Variacao variacao) {
        return pedidoReservadoRepository.somarQuantidadePorVariacaoEStatus(variacao, StatusPedido.RESERVADO);
    }

    public int disponivelSeguro(Variacao variacao) {
        int saldo = saldoMovimentacoes(variacao);
        int producaoGarantidaPendente = producaoGarantidaPendente(variacao);
        int pedidosReservados = pedidosReservados(variacao);
        int reservaSeguranca = variacao.getReservaSeguranca() == null ? 0 : variacao.getReservaSeguranca();

        return EstoqueCalculator.disponivelSeguro(saldo, producaoGarantidaPendente, pedidosReservados,
                reservaSeguranca);
    }
}
