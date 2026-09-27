package panther_stock_management.backend.service;

public final class EstoqueCalculator {

    private EstoqueCalculator() {
    }

    public static int disponivelSeguro(int saldoMovimentacoes, int producaoGarantidaPendente,
            int pedidosReservados, int reservaSeguranca) {
        return saldoMovimentacoes + producaoGarantidaPendente - pedidosReservados - reservaSeguranca;
    }
}
