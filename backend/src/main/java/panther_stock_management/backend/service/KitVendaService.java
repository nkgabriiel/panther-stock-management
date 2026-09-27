package panther_stock_management.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import panther_stock_management.backend.domain.ComposicaoKit;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.ComposicaoKitRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;

@Service
public class KitVendaService {

    private final ComposicaoKitRepository composicaoKitRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final EstoqueService estoqueService;

    public KitVendaService(ComposicaoKitRepository composicaoKitRepository,
            MovimentacaoRepository movimentacaoRepository, EstoqueService estoqueService) {
        this.composicaoKitRepository = composicaoKitRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.estoqueService = estoqueService;
    }

    public void venderKit(Variacao kitVariacao, int quantidadeKits, LocalDate data) {
        List<ComposicaoKit> composicao = composicaoKitRepository.findByKitVariacao(kitVariacao);

        for (ComposicaoKit item : composicao) {
            int quantidadeNecessaria = item.getQuantidade() * quantidadeKits;
            int estoqueDisponivel = estoqueService.saldoMovimentacoes(item.getVariacaoBase());

            if (estoqueDisponivel < quantidadeNecessaria) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente de " + item.getVariacaoBase().getNome() + " para montar "
                                + quantidadeKits + " unidade(s) de " + kitVariacao.getNome());
            }
        }

        for (ComposicaoKit item : composicao) {
            int quantidadeNecessaria = item.getQuantidade() * quantidadeKits;

            Movimentacao movimentacao = Movimentacao.builder()
                    .variacao(item.getVariacaoBase())
                    .tipo(TipoMovimentacao.VENDA)
                    .quantidade(quantidadeNecessaria)
                    .data(data)
                    .build();

            movimentacaoRepository.save(movimentacao);
        }
    }
}
