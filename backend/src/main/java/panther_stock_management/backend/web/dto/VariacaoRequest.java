package panther_stock_management.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VariacaoRequest(
        @NotNull Long produtoId,
        @NotBlank String nome,
        @NotNull Integer reservaSeguranca,
        Integer estoqueAnunciado) {
}
