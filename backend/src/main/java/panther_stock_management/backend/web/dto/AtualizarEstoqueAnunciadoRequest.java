package panther_stock_management.backend.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarEstoqueAnunciadoRequest(
        @NotNull @Min(0) Integer estoqueAnunciado) {
}
