package panther_stock_management.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import panther_stock_management.backend.domain.PedidoReservado;
import panther_stock_management.backend.domain.StatusPedido;
import panther_stock_management.backend.domain.Variacao;

public interface PedidoReservadoRepository extends JpaRepository<PedidoReservado, Long> {

    @Query("select coalesce(sum(p.quantidade), 0) from PedidoReservado p "
            + "where p.variacao = :variacao and p.status = :status")
    int somarQuantidadePorVariacaoEStatus(@Param("variacao") Variacao variacao, @Param("status") StatusPedido status);
}
