package panther_stock_management.backend.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.VariacaoRepository;
import panther_stock_management.backend.service.EstoqueService;
import panther_stock_management.backend.web.dto.MovimentacaoRequest;
import panther_stock_management.backend.web.dto.MovimentacaoResponse;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private static final LocalDate DATA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate DATA_MAXIMA = LocalDate.of(2999, 12, 31);

    private final MovimentacaoRepository movimentacaoRepository;
    private final VariacaoRepository variacaoRepository;
    private final EstoqueService estoqueService;

    public MovimentacaoController(MovimentacaoRepository movimentacaoRepository,
            VariacaoRepository variacaoRepository, EstoqueService estoqueService) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.variacaoRepository = variacaoRepository;
        this.estoqueService = estoqueService;
    }

    @PostMapping
    public ResponseEntity<MovimentacaoResponse> lancar(@Valid @RequestBody MovimentacaoRequest request) {
        Variacao variacao = variacaoRepository.findById(request.variacaoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variação não encontrada"));

        Movimentacao movimentacao = Movimentacao.builder()
                .variacao(variacao)
                .data(request.data())
                .tipo(request.tipo())
                .quantidade(request.quantidade())
                .build();
        movimentacao = movimentacaoRepository.save(movimentacao);

        int saldoAtualizado = estoqueService.saldoMovimentacoes(variacao);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(movimentacao, saldoAtualizado));
    }

    @GetMapping
    public List<MovimentacaoResponse> listar(
            @RequestParam(required = false) Long produtoId,
            @RequestParam(required = false) Long variacaoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        LocalDate inicio = dataInicio == null ? DATA_MINIMA : dataInicio;
        LocalDate fim = dataFim == null ? DATA_MAXIMA : dataFim;

        return movimentacaoRepository.buscarComFiltros(produtoId, variacaoId, inicio, fim).stream()
                .map(m -> toResponse(m, null))
                .toList();
    }

    private MovimentacaoResponse toResponse(Movimentacao movimentacao, Integer saldoAtualizado) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getVariacao().getId(),
                movimentacao.getData(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                saldoAtualizado);
    }
}
