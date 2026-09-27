package panther_stock_management.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import panther_stock_management.backend.domain.ComposicaoKit;
import panther_stock_management.backend.domain.Variacao;

public interface ComposicaoKitRepository extends JpaRepository<ComposicaoKit, Long> {

    List<ComposicaoKit> findByKitVariacao(Variacao kitVariacao);
}
