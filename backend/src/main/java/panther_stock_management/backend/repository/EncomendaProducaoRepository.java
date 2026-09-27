package panther_stock_management.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import panther_stock_management.backend.domain.EncomendaProducao;
import panther_stock_management.backend.domain.StatusEncomenda;
import panther_stock_management.backend.domain.Variacao;

public interface EncomendaProducaoRepository extends JpaRepository<EncomendaProducao, Long> {

    @Query("select coalesce(sum(e.quantidade), 0) from EncomendaProducao e "
            + "where e.variacao = :variacao and e.status = :status")
    int somarQuantidadePorVariacaoEStatus(@Param("variacao") Variacao variacao,
            @Param("status") StatusEncomenda status);

    boolean existsByVariacao(Variacao variacao);
}
