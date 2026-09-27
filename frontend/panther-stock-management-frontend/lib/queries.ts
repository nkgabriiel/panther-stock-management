import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "./api-client";
import type {
  CriarMovimentacaoInput,
  CriarProdutoInput,
  CriarVariacaoInput,
  HistoricoFiltro,
} from "./types";

export const queryKeys = {
  produtos: ["produtos"] as const,
  variacoes: ["variacoes"] as const,
  movimentacoes: (filtro?: HistoricoFiltro) => ["movimentacoes", filtro ?? {}] as const,
};

export function useProdutos() {
  return useQuery({
    queryKey: queryKeys.produtos,
    queryFn: api.produtos.listar,
  });
}

export function useVariacoes() {
  return useQuery({
    queryKey: queryKeys.variacoes,
    queryFn: api.variacoes.listar,
    refetchInterval: 30_000,
  });
}

export function useMovimentacoes(filtro?: HistoricoFiltro) {
  return useQuery({
    queryKey: queryKeys.movimentacoes(filtro),
    queryFn: () => api.movimentacoes.listar(filtro),
  });
}

export function useCriarProduto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CriarProdutoInput) => api.produtos.criar(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.produtos });
    },
  });
}

export function useCriarVariacao() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CriarVariacaoInput) => api.variacoes.criar(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.variacoes });
    },
  });
}

export function useAtualizarEstoqueAnunciado() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, estoqueAnunciado }: { id: number; estoqueAnunciado: number }) =>
      api.variacoes.atualizarEstoqueAnunciado(id, estoqueAnunciado),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.variacoes });
    },
  });
}

export function useLancarMovimentacao() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CriarMovimentacaoInput) => api.movimentacoes.lancar(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.variacoes });
      queryClient.invalidateQueries({ queryKey: ["movimentacoes"] });
    },
  });
}
