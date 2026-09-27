package panther_stock_management.backend.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import panther_stock_management.backend.domain.EncomendaProducao;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.StatusEncomenda;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.EncomendaProducaoRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EncomendaProducaoServiceTest {

    @Mock
    private EncomendaProducaoRepository encomendaProducaoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    private EncomendaProducaoService service;

    private final LocalDate data = LocalDate.of(2026, 9, 26);

    @Test
    void pendenteParaRecebidaGeraMovimentacaoProducaoRecebidaComQuantidadeCorreta() {
        service = new EncomendaProducaoService(encomendaProducaoRepository, movimentacaoRepository);

        Variacao preto = Variacao.builder().id(1L).nome("Preto").build();
        EncomendaProducao encomenda = EncomendaProducao.builder()
                .id(1L)
                .variacao(preto)
                .quantidade(24)
                .status(StatusEncomenda.PENDENTE)
                .build();

        service.transicionarPara(encomenda, StatusEncomenda.RECEBIDA, data);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(captor.capture());

        Movimentacao movimentacao = captor.getValue();
        assertEquals(TipoMovimentacao.PRODUCAO_RECEBIDA, movimentacao.getTipo());
        assertEquals(24, movimentacao.getQuantidade());
        assertEquals(preto, movimentacao.getVariacao());
        assertEquals(StatusEncomenda.RECEBIDA, encomenda.getStatus());
    }

    @Test
    void marcarEncomendaJaRecebidaComoRecebidaNovamenteNaoDuplicaMovimentacao() {
        service = new EncomendaProducaoService(encomendaProducaoRepository, movimentacaoRepository);

        EncomendaProducao encomenda = EncomendaProducao.builder()
                .id(1L)
                .variacao(Variacao.builder().id(1L).nome("Preto").build())
                .quantidade(24)
                .status(StatusEncomenda.RECEBIDA)
                .build();

        service.transicionarPara(encomenda, StatusEncomenda.RECEBIDA, data);

        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void transicaoDeRecebidaParaPendenteEhRejeitada() {
        service = new EncomendaProducaoService(encomendaProducaoRepository, movimentacaoRepository);

        EncomendaProducao encomenda = EncomendaProducao.builder()
                .id(1L)
                .variacao(Variacao.builder().id(1L).nome("Preto").build())
                .quantidade(24)
                .status(StatusEncomenda.RECEBIDA)
                .build();

        assertThrows(IllegalStateException.class,
                () -> service.transicionarPara(encomenda, StatusEncomenda.PENDENTE, data));

        verify(movimentacaoRepository, never()).save(any());
    }
}
