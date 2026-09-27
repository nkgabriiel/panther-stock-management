package panther_stock_management.backend.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import panther_stock_management.backend.domain.Produto;
import panther_stock_management.backend.repository.ProdutoRepository;
import panther_stock_management.backend.repository.VariacaoRepository;
import panther_stock_management.backend.web.dto.ProdutoRequest;
import panther_stock_management.backend.web.dto.ProdutoResponse;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final VariacaoRepository variacaoRepository;

    public ProdutoController(ProdutoRepository produtoRepository, VariacaoRepository variacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.variacaoRepository = variacaoRepository;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        Produto produto = Produto.builder().nome(request.nome()).tipo(request.tipo()).build();
        produto = produtoRepository.save(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoResponse.de(produto));
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAll().stream().map(ProdutoResponse::de).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        if (variacaoRepository.existsByProduto(produto)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir: exclua as variações deste produto primeiro");
        }

        produtoRepository.delete(produto);
        return ResponseEntity.noContent().build();
    }
}
