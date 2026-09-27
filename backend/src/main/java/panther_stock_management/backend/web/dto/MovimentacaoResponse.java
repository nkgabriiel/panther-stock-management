package panther_stock_management.backend.web.dto;

import java.time.LocalDate;

import panther_stock_management.backend.domain.TipoMovimentacao;

public record MovimentacaoResponse(
        Long id,
        Long variacaoId,
        LocalDate data,
        TipoMovimentacao tipo,
        Integer quantidade,
        Integer saldoAtualizado) {
}
