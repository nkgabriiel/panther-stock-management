package panther_stock_management.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.TipoMovimentacao;
import panther_stock_management.backend.domain.Variacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    @Query("select coalesce(sum(m.quantidade), 0) from Movimentacao m "
            + "where m.variacao = :variacao and m.tipo = :tipo")
    int somarQuantidadePorVariacaoETipo(@Param("variacao") Variacao variacao, @Param("tipo") TipoMovimentacao tipo);

    @Query("select m from Movimentacao m join m.variacao v join v.produto p "
            + "where (:produtoId is null or p.id = :produtoId) "
            + "and (:variacaoId is null or v.id = :variacaoId) "
            + "and m.data >= :dataInicio and m.data <= :dataFim "
            + "order by m.data desc, m.id desc")
    List<Movimentacao> buscarComFiltros(@Param("produtoId") Long produtoId, @Param("variacaoId") Long variacaoId,
            @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
