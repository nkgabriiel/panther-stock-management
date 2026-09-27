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
  configuracaoBackup: ["configuracaoBackup"] as const,
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

export function useExcluirProduto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.produtos.excluir(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.produtos });
      queryClient.invalidateQueries({ queryKey: queryKeys.variacoes });
    },
  });
}

export function useExcluirVariacao() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.variacoes.excluir(id),
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

export function useConfiguracaoBackup() {
  return useQuery({
    queryKey: queryKeys.configuracaoBackup,
    queryFn: api.backup.obterConfiguracao,
  });
}

export function useAtualizarConfiguracaoBackup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.backup.atualizarConfiguracao,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.configuracaoBackup });
    },
  });
}

export function useGerarBackup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.backup.gerar,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.configuracaoBackup });
    },
  });
}

export function useImportarBackup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.backup.importar,
    onSuccess: () => {
      queryClient.invalidateQueries();
    },
  });
}
