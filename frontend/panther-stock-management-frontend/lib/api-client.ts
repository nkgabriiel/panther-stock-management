import type {
  ApiErrorBody,
  CriarMovimentacaoInput,
  CriarProdutoInput,
  CriarVariacaoInput,
  HistoricoFiltro,
  Movimentacao,
  Produto,
  Variacao,
} from "./types";

export class ApiError extends Error {
  status: number;
  body: ApiErrorBody;

  constructor(status: number, body: ApiErrorBody) {
    super(body.mensagem ?? `Erro na requisição (${status})`);
    this.status = status;
    this.body = body;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/backend/${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ApiErrorBody;
    throw new ApiError(response.status, body);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  produtos: {
    listar: () => request<Produto[]>("produtos"),
    criar: (input: CriarProdutoInput) =>
      request<Produto>("produtos", { method: "POST", body: JSON.stringify(input) }),
  },
  variacoes: {
    listar: () => request<Variacao[]>("variacoes"),
    buscar: (id: number) => request<Variacao>(`variacoes/${id}`),
    criar: (input: CriarVariacaoInput) =>
      request<Variacao>("variacoes", { method: "POST", body: JSON.stringify(input) }),
    atualizarEstoqueAnunciado: (id: number, estoqueAnunciado: number) =>
      request<Variacao>(`variacoes/${id}/estoque-anunciado`, {
        method: "PATCH",
        body: JSON.stringify({ estoqueAnunciado }),
      }),
  },
  movimentacoes: {
    listar: (filtro?: HistoricoFiltro) => {
      const params = new URLSearchParams();
      if (filtro?.produtoId) params.set("produtoId", String(filtro.produtoId));
      if (filtro?.variacaoId) params.set("variacaoId", String(filtro.variacaoId));
      if (filtro?.dataInicio) params.set("dataInicio", filtro.dataInicio);
      if (filtro?.dataFim) params.set("dataFim", filtro.dataFim);
      const query = params.toString();
      return request<Movimentacao[]>(`movimentacoes${query ? `?${query}` : ""}`);
    },
    lancar: (input: CriarMovimentacaoInput) =>
      request<Movimentacao>("movimentacoes", { method: "POST", body: JSON.stringify(input) }),
  },
};
