package panther_stock_management.backend.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import panther_stock_management.backend.domain.Produto;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.ProdutoRepository;
import panther_stock_management.backend.repository.VariacaoRepository;
import panther_stock_management.backend.service.AnuncioCalculator;
import panther_stock_management.backend.service.EstoqueCalculator;
import panther_stock_management.backend.service.EstoqueService;
import panther_stock_management.backend.web.dto.AtualizarEstoqueAnunciadoRequest;
import panther_stock_management.backend.web.dto.VariacaoRequest;
import panther_stock_management.backend.web.dto.VariacaoResponse;

@RestController
@RequestMapping("/api/variacoes")
public class VariacaoController {

    private final VariacaoRepository variacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;

    public VariacaoController(VariacaoRepository variacaoRepository, ProdutoRepository produtoRepository,
            EstoqueService estoqueService) {
        this.variacaoRepository = variacaoRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueService = estoqueService;
    }

    @PostMapping
    public ResponseEntity<VariacaoResponse> criar(@Valid @RequestBody VariacaoRequest request) {
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        Variacao variacao = Variacao.builder()
                .produto(produto)
                .nome(request.nome())
                .reservaSeguranca(request.reservaSeguranca())
                .estoqueAnunciado(request.estoqueAnunciado() == null ? 0 : request.estoqueAnunciado())
                .build();
        variacao = variacaoRepository.save(variacao);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(variacao));
    }

    @GetMapping("/{id}")
    public VariacaoResponse buscar(@PathVariable Long id) {
        Variacao variacao = variacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variação não encontrada"));
        return toResponse(variacao);
    }

    @GetMapping
    public List<VariacaoResponse> listar() {
        return variacaoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/estoque-anunciado")
    public VariacaoResponse atualizarEstoqueAnunciado(@PathVariable Long id,
            @Valid @RequestBody AtualizarEstoqueAnunciadoRequest request) {
        Variacao variacao = variacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variação não encontrada"));

        variacao.setEstoqueAnunciado(request.estoqueAnunciado());
        variacao = variacaoRepository.save(variacao);

        return toResponse(variacao);
    }

    private VariacaoResponse toResponse(Variacao variacao) {
        int pronto = estoqueService.saldoMovimentacoes(variacao);
        int producaoGarantida = estoqueService.producaoGarantidaPendente(variacao);
        int pedidosReservados = estoqueService.pedidosReservados(variacao);
        int reservaSeguranca = variacao.getReservaSeguranca() == null ? 0 : variacao.getReservaSeguranca();
        int disponivelSeguro = EstoqueCalculator.disponivelSeguro(pronto, producaoGarantida, pedidosReservados,
                reservaSeguranca);
        int estoqueAnunciado = variacao.getEstoqueAnunciado() == null ? 0 : variacao.getEstoqueAnunciado();
        int folga = AnuncioCalculator.folga(disponivelSeguro, estoqueAnunciado);

        return new VariacaoResponse(variacao.getId(), variacao.getProduto().getId(), variacao.getNome(),
                variacao.getReservaSeguranca(), pronto, producaoGarantida, pedidosReservados, disponivelSeguro,
                estoqueAnunciado, folga, AnuncioCalculator.status(folga));
    }
}
