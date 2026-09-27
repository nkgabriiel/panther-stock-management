package panther_stock_management.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstoqueCalculatorTest {

    @Test
    void producaoGarantidaPendenteSomaCorretamenteAoDisponivel() {
        int resultado = EstoqueCalculator.disponivelSeguro(100, 20, 0, 0);

        assertEquals(120, resultado);
    }

    @Test
    void pedidosReservadosReduzemCorretamenteODisponivel() {
        int resultado = EstoqueCalculator.disponivelSeguro(100, 0, 30, 0);

        assertEquals(70, resultado);
    }

    @Test
    void reservaDeSegurancaReduzDisponivel() {
        int resultado = EstoqueCalculator.disponivelSeguro(100, 0, 0, 15);

        assertEquals(85, resultado);
    }

    @Test
    void disponivelPodeFicarNegativoQuandoReservasEPedidosExcedemOEstoquePronto() {
        int resultado = EstoqueCalculator.disponivelSeguro(10, 0, 25, 5);

        assertEquals(-20, resultado);
    }

    @Test
    void disponivelPodeFicarZero() {
        int resultado = EstoqueCalculator.disponivelSeguro(10, 0, 10, 0);

        assertEquals(0, resultado);
    }

    @Test
    void variacaoRecemCriadaSemMovimentacaoTemDisponivelZero() {
        int resultado = EstoqueCalculator.disponivelSeguro(0, 0, 0, 0);

        assertEquals(0, resultado);
    }

    @Test
    void reproduzOsNumerosDoPainelDeReferenciaParaAsTresCores() {
        assertEquals(108, EstoqueCalculator.disponivelSeguro(120, 40, 22, 30));
        assertEquals(32, EstoqueCalculator.disponivelSeguro(45, 20, 18, 15));
        assertEquals(4, EstoqueCalculator.disponivelSeguro(28, 0, 9, 15));
    }
}
