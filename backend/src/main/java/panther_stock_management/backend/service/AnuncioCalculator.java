package panther_stock_management.backend.service;

import panther_stock_management.backend.domain.StatusAnuncio;

public final class AnuncioCalculator {

    private AnuncioCalculator() {
    }

    public static int folga(int disponivelSeguro, int estoqueAnunciado) {
        return disponivelSeguro - estoqueAnunciado;
    }

    public static StatusAnuncio status(int folga) {
        if (folga > 0) {
            return StatusAnuncio.PODE_ANUNCIAR;
        }
        if (folga == 0) {
            return StatusAnuncio.NO_LIMITE;
        }
        return StatusAnuncio.REDUZIR;
    }
}
