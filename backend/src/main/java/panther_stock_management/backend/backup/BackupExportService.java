package panther_stock_management.backend.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import panther_stock_management.backend.domain.ComposicaoKit;
import panther_stock_management.backend.domain.EncomendaProducao;
import panther_stock_management.backend.domain.Movimentacao;
import panther_stock_management.backend.domain.PedidoReservado;
import panther_stock_management.backend.domain.Produto;
import panther_stock_management.backend.domain.Variacao;
import panther_stock_management.backend.repository.ComposicaoKitRepository;
import panther_stock_management.backend.repository.EncomendaProducaoRepository;
import panther_stock_management.backend.repository.MovimentacaoRepository;
import panther_stock_management.backend.repository.PedidoReservadoRepository;
import panther_stock_management.backend.repository.ProdutoRepository;
import panther_stock_management.backend.repository.VariacaoRepository;

@Service
public class BackupExportService {

    static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ProdutoRepository produtoRepository;
    private final VariacaoRepository variacaoRepository;
    private final ComposicaoKitRepository composicaoKitRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final EncomendaProducaoRepository encomendaProducaoRepository;
    private final PedidoReservadoRepository pedidoReservadoRepository;

    public BackupExportService(ProdutoRepository produtoRepository, VariacaoRepository variacaoRepository,
            ComposicaoKitRepository composicaoKitRepository, MovimentacaoRepository movimentacaoRepository,
            EncomendaProducaoRepository encomendaProducaoRepository,
            PedidoReservadoRepository pedidoReservadoRepository) {
        this.produtoRepository = produtoRepository;
        this.variacaoRepository = variacaoRepository;
        this.composicaoKitRepository = composicaoKitRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.encomendaProducaoRepository = encomendaProducaoRepository;
        this.pedidoReservadoRepository = pedidoReservadoRepository;
    }

    public byte[] gerarPlanilha() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            escreverProdutos(workbook);
            escreverVariacoes(workbook);
            escreverComposicaoKit(workbook);
            escreverMovimentacoes(workbook);
            escreverEncomendasProducao(workbook);
            escreverPedidosReservados(workbook);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar planilha de backup", e);
        }
    }

    private void escreverProdutos(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(BackupSheets.PRODUTOS);
        escreverCabecalho(sheet, "id", "nome", "tipo");

        int linha = 1;
        for (Produto produto : produtoRepository.findAll()) {
            Row row = sheet.createRow(linha++);
            row.createCell(0).setCellValue(produto.getId());
            row.createCell(1).setCellValue(produto.getNome());
            row.createCell(2).setCellValue(produto.getTipo().name());
        }
    }

    private void escreverVariacoes(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(BackupSheets.VARIACOES);
        escreverCabecalho(sheet, "id", "produtoId", "nome", "reservaSeguranca", "estoqueAnunciado");

        int linha = 1;
        for (Variacao variacao : variacaoRepository.findAll()) {
            Row row = sheet.createRow(linha++);
            row.createCell(0).setCellValue(variacao.getId());
            row.createCell(1).setCellValue(variacao.getProduto().getId());
            row.createCell(2).setCellValue(variacao.getNome());
            row.createCell(3).setCellValue(
                    variacao.getReservaSeguranca() == null ? 0 : variacao.getReservaSeguranca());
            row.createCell(4).setCellValue(
                    variacao.getEstoqueAnunciado() == null ? 0 : variacao.getEstoqueAnunciado());
        }
    }

    private void escreverComposicaoKit(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(BackupSheets.COMPOSICAO_KIT);
        escreverCabecalho(sheet, "id", "kitVariacaoId", "variacaoBaseId", "quantidade");

        int linha = 1;
        for (ComposicaoKit item : composicaoKitRepository.findAll()) {
            Row row = sheet.createRow(linha++);
            row.createCell(0).setCellValue(item.getId());
            row.createCell(1).setCellValue(item.getKitVariacao().getId());
            row.createCell(2).setCellValue(item.getVariacaoBase().getId());
            row.createCell(3).setCellValue(item.getQuantidade());
        }
    }

    private void escreverMovimentacoes(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(BackupSheets.MOVIMENTACOES);
        escreverCabecalho(sheet, "id", "variacaoId", "data", "tipo", "quantidade");

        int linha = 1;
        for (Movimentacao movimentacao : movimentacaoRepository.findAll()) {
            Row row = sheet.createRow(linha++);
            row.createCell(0).setCellValue(movimentacao.getId());
            row.createCell(1).setCellValue(movimentacao.getVariacao().getId());
            escreverData(row.createCell(2), movimentacao.getData());
            row.createCell(3).setCellValue(movimentacao.getTipo().name());
            row.createCell(4).setCellValue(movimentacao.getQuantidade());
        }
    }

    private void escreverEncomendasProducao(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(BackupSheets.ENCOMENDAS_PRODUCAO);
        escreverCabecalho(sheet, "id", "variacaoId", "quantidade", "dataPrevista", "status");

        int linha = 1;
        for (EncomendaProducao encomenda : encomendaProducaoRepository.findAll()) {
            Row row = sheet.createRow(linha++);
            row.createCell(0).setCellValue(encomenda.getId());
            row.createCell(1).setCellValue(encomenda.getVariacao().getId());
            row.createCell(2).setCellValue(encomenda.getQuantidade());
            escreverData(row.createCell(3), encomenda.getDataPrevista());
            row.createCell(4).setCellValue(encomenda.getStatus().name());
        }
    }

    private void escreverPedidosReservados(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(BackupSheets.PEDIDOS_RESERVADOS);
        escreverCabecalho(sheet, "id", "variacaoId", "quantidade", "dataVenda", "status");

        int linha = 1;
        for (PedidoReservado pedido : pedidoReservadoRepository.findAll()) {
            Row row = sheet.createRow(linha++);
            row.createCell(0).setCellValue(pedido.getId());
            row.createCell(1).setCellValue(pedido.getVariacao().getId());
            row.createCell(2).setCellValue(pedido.getQuantidade());
            escreverData(row.createCell(3), pedido.getDataVenda());
            row.createCell(4).setCellValue(pedido.getStatus().name());
        }
    }

    private void escreverCabecalho(XSSFSheet sheet, String... colunas) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < colunas.length; i++) {
            header.createCell(i).setCellValue(colunas[i]);
        }
    }

    private void escreverData(Cell cell, java.time.LocalDate data) {
        if (data != null) {
            cell.setCellValue(data.format(DATA_FORMATO));
        }
    }
}
