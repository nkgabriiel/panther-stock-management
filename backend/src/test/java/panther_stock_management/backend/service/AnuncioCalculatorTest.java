package panther_stock_management.backend.service;

import org.junit.jupiter.api.Test;

import panther_stock_management.backend.domain.StatusAnuncio;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnuncioCalculatorTest {

    @Test
    void folgaPositivaResultaEmPodeAnunciar() {
        int folga = AnuncioCalculator.folga(108, 90);

        assertEquals(18, folga);
        assertEquals(StatusAnuncio.PODE_ANUNCIAR, AnuncioCalculator.status(folga));
    }

    @Test
    void folgaNegativaResultaEmReduzir() {
        int folga = AnuncioCalculator.folga(32, 40);

        assertEquals(-8, folga);
        assertEquals(StatusAnuncio.REDUZIR, AnuncioCalculator.status(folga));
    }

    @Test
    void folgaZeraResultaEmNoLimite() {
        int folga = AnuncioCalculator.folga(4, 4);

        assertEquals(0, folga);
        assertEquals(StatusAnuncio.NO_LIMITE, AnuncioCalculator.status(folga));
    }
}
