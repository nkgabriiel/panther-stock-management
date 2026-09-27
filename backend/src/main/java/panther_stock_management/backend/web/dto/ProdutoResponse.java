package panther_stock_management.backend.web.dto;

import panther_stock_management.backend.domain.Produto;
import panther_stock_management.backend.domain.TipoProduto;

public record ProdutoResponse(Long id, String nome, TipoProduto tipo) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getTipo());
    }
}
