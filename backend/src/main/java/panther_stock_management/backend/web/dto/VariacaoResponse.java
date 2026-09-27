package panther_stock_management.backend.web.dto;

import panther_stock_management.backend.domain.StatusAnuncio;

public record VariacaoResponse(
        Long id,
        Long produtoId,
        String nome,
        Integer reservaSeguranca,
        Integer pronto,
        Integer producaoGarantida,
        Integer pedidosReservados,
        Integer disponivelSeguro,
        Integer estoqueAnunciado,
        Integer folga,
        StatusAnuncio statusAnuncio) {
}
