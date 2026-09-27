package panther_stock_management.backend.backup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Service
public class BackupImportService {

    private static final String[] TABELAS_EM_ORDEM_DE_EXCLUSAO = {
            "composicao_kit", "movimentacao", "encomenda_producao", "pedido_reservado", "variacao", "produto"
    };

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public BackupImportService(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @Transactional
    public void importar(byte[] planilha) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(planilha))) {
            List<Object[]> produtos = lerLinhas(workbook.getSheet(BackupSheets.PRODUTOS), 3);
            List<Object[]> variacoes = lerLinhas(workbook.getSheet(BackupSheets.VARIACOES), 5);
            List<Object[]> composicaoKit = lerLinhas(workbook.getSheet(BackupSheets.COMPOSICAO_KIT), 4);
            List<Object[]> movimentacoes = lerLinhas(workbook.getSheet(BackupSheets.MOVIMENTACOES), 5);
            List<Object[]> encomendas = lerLinhas(workbook.getSheet(BackupSheets.ENCOMENDAS_PRODUCAO), 5);
            List<Object[]> pedidos = lerLinhas(workbook.getSheet(BackupSheets.PEDIDOS_RESERVADOS), 5);

            entityManager.flush();
            entityManager.clear();

            for (String tabela : TABELAS_EM_ORDEM_DE_EXCLUSAO) {
                jdbcTemplate.update("delete from " + tabela);
            }

            for (Object[] linha : produtos) {
                jdbcTemplate.update("insert into produto (id, nome, tipo) values (?, ?, ?)",
                        linha[0], linha[1], linha[2]);
            }
            for (Object[] linha : variacoes) {
                jdbcTemplate.update("insert into variacao "
                        + "(id, produto_id, nome, reserva_seguranca, estoque_anunciado) values (?, ?, ?, ?, ?)",
                        linha[0], linha[1], linha[2], linha[3], linha[4]);
            }
            for (Object[] linha : composicaoKit) {
                jdbcTemplate.update("insert into composicao_kit "
                        + "(id, kit_variacao_id, variacao_base_id, quantidade) values (?, ?, ?, ?)",
                        linha[0], linha[1], linha[2], linha[3]);
            }
            for (Object[] linha : movimentacoes) {
                jdbcTemplate.update("insert into movimentacao "
                        + "(id, variacao_id, data, tipo, quantidade) values (?, ?, ?, ?, ?)",
                        linha[0], linha[1], linha[2], linha[3], linha[4]);
            }
            for (Object[] linha : encomendas) {
                jdbcTemplate.update("insert into encomenda_producao "
                        + "(id, variacao_id, quantidade, data_prevista, status) values (?, ?, ?, ?, ?)",
                        linha[0], linha[1], linha[2], linha[3], linha[4]);
            }
            for (Object[] linha : pedidos) {
                jdbcTemplate.update("insert into pedido_reservado "
                        + "(id, variacao_id, quantidade, data_venda, status) values (?, ?, ?, ?, ?)",
                        linha[0], linha[1], linha[2], linha[3], linha[4]);
            }

            for (String tabela : TABELAS_EM_ORDEM_DE_EXCLUSAO) {
                reiniciarSequencia(tabela);
            }

            entityManager.clear();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler planilha de backup", e);
        }
    }

    private void reiniciarSequencia(String tabela) {
        jdbcTemplate.queryForObject(
                "select setval(pg_get_serial_sequence(?, 'id'), coalesce((select max(id) from " + tabela
                        + "), 1), (select max(id) from " + tabela + ") is not null)",
                Long.class, tabela);
    }

    private List<Object[]> lerLinhas(Sheet sheet, int colunas) {
        List<Object[]> linhas = new ArrayList<>();
        if (sheet == null) {
            return linhas;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) {
                continue;
            }
            Object[] valores = new Object[colunas];
            for (int c = 0; c < colunas; c++) {
                valores[c] = lerCelula(row, c);
            }
            linhas.add(valores);
        }
        return linhas;
    }

    private Object lerCelula(Row row, int coluna) {
        var cell = row.getCell(coluna);
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> converterSeForData(cell.getStringCellValue());
            default -> null;
        };
    }

    private Object converterSeForData(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            LocalDate data = LocalDate.parse(valor, BackupExportService.DATA_FORMATO);
            return Timestamp.valueOf(data.atStartOfDay()).toLocalDateTime().toLocalDate();
        } catch (Exception notADate) {
            return valor;
        }
    }
}
