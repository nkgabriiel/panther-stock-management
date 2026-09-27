package panther_stock_management.backend.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import panther_stock_management.backend.domain.TipoMovimentacao;

public record MovimentacaoRequest(
        @NotNull Long variacaoId,
        @NotNull LocalDate data,
        @NotNull TipoMovimentacao tipo,
        @NotNull Integer quantidade) {
}
