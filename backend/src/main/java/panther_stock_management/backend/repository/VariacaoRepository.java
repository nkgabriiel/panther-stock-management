package panther_stock_management.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import panther_stock_management.backend.domain.Produto;
import panther_stock_management.backend.domain.Variacao;

public interface VariacaoRepository extends JpaRepository<Variacao, Long> {

    boolean existsByProduto(Produto produto);
}
