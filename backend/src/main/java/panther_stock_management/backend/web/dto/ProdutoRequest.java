package panther_stock_management.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import panther_stock_management.backend.domain.TipoProduto;

public record ProdutoRequest(
        @NotBlank String nome,
        @NotNull TipoProduto tipo) {
}
