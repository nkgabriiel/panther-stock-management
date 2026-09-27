export type TipoProduto = "UNIDADE" | "KIT";

export type TipoMovimentacao = "PRODUCAO_RECEBIDA" | "VENDA" | "AJUSTE";

export type StatusAnuncio = "PODE_ANUNCIAR" | "NO_LIMITE" | "REDUZIR";

export interface Produto {
  id: number;
  nome: string;
  tipo: TipoProduto;
}

export interface Variacao {
  id: number;
  produtoId: number;
  nome: string;
  reservaSeguranca: number;
  pronto: number;
  producaoGarantida: number;
  pedidosReservados: number;
  disponivelSeguro: number;
  estoqueAnunciado: number;
  folga: number;
  statusAnuncio: StatusAnuncio;
}

export interface Movimentacao {
  id: number;
  variacaoId: number;
  data: string;
  tipo: TipoMovimentacao;
  quantidade: number;
  saldoAtualizado: number | null;
}

export interface CriarProdutoInput {
  nome: string;
  tipo: TipoProduto;
}

export interface CriarVariacaoInput {
  produtoId: number;
  nome: string;
  reservaSeguranca: number;
  estoqueAnunciado?: number;
}

export interface CriarMovimentacaoInput {
  variacaoId: number;
  data: string;
  tipo: TipoMovimentacao;
  quantidade: number;
}

export interface HistoricoFiltro {
  produtoId?: number;
  variacaoId?: number;
  dataInicio?: string;
  dataFim?: string;
}

export interface ApiErrorBody {
  mensagem?: string;
  erros?: Record<string, string>;
}
