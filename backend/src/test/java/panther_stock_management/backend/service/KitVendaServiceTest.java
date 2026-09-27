package panther_stock_management.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import panther_stock_management.backend.domain.ComposicaoKit;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.ComposicaoKitRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitVendaServiceTest {

    @Mock
    private ComposicaoKitRepository composicaoKitRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private EstoqueService estoqueService;

    private KitVendaService kitVendaService;

    private final LocalDate data = LocalDate.of(2026, 9, 26);

    @Test
    void vendaDeKitDescontaQuantidadeCorretaDeCadaVariacaoQueOCompoe() {
        kitVendaService = new KitVendaService(composicaoKitRepository, movimentacaoRepository, estoqueService);

        Variacao kit = Variacao.builder().id(10L).nome("1 Preto + 1 Branco + 1 Rosa").build();
        Variacao preto = Variacao.builder().id(1L).nome("Preto").build();
        Variacao branco = Variacao.builder().id(2L).nome("Branco").build();
        Variacao rosa = Variacao.builder().id(3L).nome("Rosa").build();

        ComposicaoKit itemPreto = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(preto).quantidade(1).build();
        ComposicaoKit itemBranco = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(branco).quantidade(1)
                .build();
        ComposicaoKit itemRosa = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(rosa).quantidade(1).build();

        when(composicaoKitRepository.findByKitVariacao(kit)).thenReturn(List.of(itemPreto, itemBranco, itemRosa));
        lenient().when(estoqueService.saldoMovimentacoes(any(Variacao.class))).thenReturn(100);

        kitVendaService.venderKit(kit, 5, data);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(3)).save(captor.capture());

        List<Movimentacao> salvas = captor.getAllValues();
        assertEquals(5, quantidadePara(salvas, preto));
        assertEquals(5, quantidadePara(salvas, branco));
        assertEquals(5, quantidadePara(salvas, rosa));
        salvas.forEach(m -> assertEquals(TipoMovimentacao.VENDA, m.getTipo()));
    }

    @Test
    void kitComQuantidadesDiferentesPorVariacaoDescontaProporcionalmente() {
        kitVendaService = new KitVendaService(composicaoKitRepository, movimentacaoRepository, estoqueService);

        Variacao kit = Variacao.builder().id(11L).nome("2 Pretas + 1 Branca").build();
        Variacao preto = Variacao.builder().id(1L).nome("Preto").build();
        Variacao branco = Variacao.builder().id(2L).nome("Branco").build();

        ComposicaoKit itemPreto = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(preto).quantidade(2).build();
        ComposicaoKit itemBranco = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(branco).quantidade(1)
                .build();

        when(composicaoKitRepository.findByKitVariacao(kit)).thenReturn(List.of(itemPreto, itemBranco));
        lenient().when(estoqueService.saldoMovimentacoes(any(Variacao.class))).thenReturn(100);

        kitVendaService.venderKit(kit, 3, data);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(2)).save(captor.capture());

        List<Movimentacao> salvas = captor.getAllValues();
        assertEquals(6, quantidadePara(salvas, preto));
        assertEquals(3, quantidadePara(salvas, branco));
    }

    @Test
    void kitNaoPodeSerVendidoSeAlgumaVariacaoDaComposicaoNaoTemEstoqueSuficiente() {
        kitVendaService = new KitVendaService(composicaoKitRepository, movimentacaoRepository, estoqueService);

        Variacao kit = Variacao.builder().id(12L).nome("1 Preto + 1 Branco").build();
        Variacao preto = Variacao.builder().id(1L).nome("Preto").build();
        Variacao branco = Variacao.builder().id(2L).nome("Branco").build();

        ComposicaoKit itemPreto = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(preto).quantidade(1).build();
        ComposicaoKit itemBranco = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(branco).quantidade(1)
                .build();

        when(composicaoKitRepository.findByKitVariacao(kit)).thenReturn(List.of(itemPreto, itemBranco));
        lenient().when(estoqueService.saldoMovimentacoes(preto)).thenReturn(100);
        lenient().when(estoqueService.saldoMovimentacoes(branco)).thenReturn(2);

        assertThrows(EstoqueInsuficienteException.class, () -> kitVendaService.venderKit(kit, 5, data));

        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void alterarComposicaoDoKitNaoAfetaVendasJaRegistradasAnteriormente() {
        kitVendaService = new KitVendaService(composicaoKitRepository, movimentacaoRepository, estoqueService);

        Variacao kit = Variacao.builder().id(13L).nome("3 Pretas").build();
        Variacao preto = Variacao.builder().id(1L).nome("Preto").build();

        ComposicaoKit item = ComposicaoKit.builder().kitVariacao(kit).variacaoBase(preto).quantidade(3).build();

        when(composicaoKitRepository.findByKitVariacao(kit)).thenReturn(List.of(item));
        lenient().when(estoqueService.saldoMovimentacoes(any(Variacao.class))).thenReturn(100);

        kitVendaService.venderKit(kit, 2, data);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(captor.capture());
        int quantidadeRegistrada = captor.getValue().getQuantidade();

        item.setQuantidade(10);

        assertEquals(6, quantidadeRegistrada);
    }

    private int quantidadePara(List<Movimentacao> movimentacoes, Variacao variacao) {
        return movimentacoes.stream()
                .filter(m -> m.getVariacao().equals(variacao))
                .mapToInt(Movimentacao::getQuantidade)
                .sum();
    }
}
