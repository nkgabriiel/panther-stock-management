package panther_stock_management.backend.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import panther_stock_management.backend.domain.EncomendaProducao;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.StatusEncomenda;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.repository.EncomendaProducaoRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;

@Service
public class EncomendaProducaoService {

    private final EncomendaProducaoRepository encomendaProducaoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public EncomendaProducaoService(EncomendaProducaoRepository encomendaProducaoRepository,
            MovimentacaoRepository movimentacaoRepository) {
        this.encomendaProducaoRepository = encomendaProducaoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public void transicionarPara(EncomendaProducao encomenda, StatusEncomenda novoStatus, LocalDate data) {
        StatusEncomenda atual = encomenda.getStatus();

        if (atual == novoStatus) {
            return;
        }

        if (atual != StatusEncomenda.PENDENTE || novoStatus != StatusEncomenda.RECEBIDA) {
            throw new IllegalStateException(
                    "Transição de status inválida: " + atual + " -> " + novoStatus);
        }

        Movimentacao movimentacao = Movimentacao.builder()
                .variacao(encomenda.getVariacao())
                .tipo(TipoMovimentacao.PRODUCAO_RECEBIDA)
                .quantidade(encomenda.getQuantidade())
                .data(data)
                .build();
        movimentacaoRepository.save(movimentacao);

        encomenda.setStatus(StatusEncomenda.RECEBIDA);
        encomendaProducaoRepository.save(encomenda);
    }
}
