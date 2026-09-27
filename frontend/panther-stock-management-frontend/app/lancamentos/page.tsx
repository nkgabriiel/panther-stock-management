"use client";

import { useMemo, useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useLancarMovimentacao, useMovimentacoes, useProdutos, useVariacoes } from "@/lib/queries";
import type { TipoMovimentacao } from "@/lib/types";
import { ApiError } from "@/lib/api-client";

const tipoLabels: Record<TipoMovimentacao, string> = {
  PRODUCAO_RECEBIDA: "Produção recebida",
  VENDA: "Venda",
  AJUSTE: "Ajuste",
};

function hoje() {
  return new Date().toISOString().slice(0, 10);
}

export default function LancamentosPage() {
  const produtosQuery = useProdutos();
  const variacoesQuery = useVariacoes();
  const movimentacoesQuery = useMovimentacoes();
  const lancar = useLancarMovimentacao();

  const [variacaoId, setVariacaoId] = useState<string>("");
  const [tipo, setTipo] = useState<TipoMovimentacao>("PRODUCAO_RECEBIDA");
  const [quantidade, setQuantidade] = useState("");
  const [data, setData] = useState(hoje());

  const produtosPorId = useMemo(() => {
    const mapa = new Map<number, string>();
    for (const produto of produtosQuery.data ?? []) mapa.set(produto.id, produto.nome);
    return mapa;
  }, [produtosQuery.data]);

  const variacoesPorId = useMemo(() => {
    const mapa = new Map<number, string>();
    for (const variacao of variacoesQuery.data ?? [])
      mapa.set(variacao.id, `${produtosPorId.get(variacao.produtoId) ?? "?"} · ${variacao.nome}`);
    return mapa;
  }, [variacoesQuery.data, produtosPorId]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (!variacaoId) {
      toast.error("Selecione uma variação");
      return;
    }
    const numero = Number(quantidade);
    if (!Number.isFinite(numero) || numero <= 0) {
      toast.error("Informe uma quantidade válida");
      return;
    }

    try {
      const resultado = await lancar.mutateAsync({
        variacaoId: Number(variacaoId),
        tipo,
        quantidade: numero,
        data,
      });
      toast.success(
        `Lançado: ${tipoLabels[tipo]} de ${numero}. Saldo atualizado: ${resultado.saldoAtualizado}`,
      );
      setQuantidade("");
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao lançar movimentação");
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">Controle diário de estoque</h1>
        <p className="text-sm text-muted-foreground">
          Registre produção recebida, vendas avulsas e ajustes de inventário.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Nova movimentação</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-5">
            <div className="flex flex-col gap-2 sm:col-span-2">
              <Label htmlFor="variacao">Produto / Variação</Label>
              <Select value={variacaoId} onValueChange={(value) => setVariacaoId(value ?? "")}>
                <SelectTrigger id="variacao" className="w-full">
                  <SelectValue placeholder="Selecione">
                    {(value: string | null) =>
                      value ? variacoesPorId.get(Number(value)) : "Selecione"
                    }
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {(variacoesQuery.data ?? []).map((variacao) => (
                    <SelectItem key={variacao.id} value={String(variacao.id)}>
                      {variacoesPorId.get(variacao.id)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="tipo">Tipo</Label>
              <Select value={tipo} onValueChange={(value) => setTipo(value as TipoMovimentacao)}>
                <SelectTrigger id="tipo" className="w-full">
                  <SelectValue>
                    {(value: TipoMovimentacao | null) => (value ? tipoLabels[value] : "Selecione")}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PRODUCAO_RECEBIDA">Produção recebida</SelectItem>
                  <SelectItem value="VENDA">Venda</SelectItem>
                  <SelectItem value="AJUSTE">Ajuste</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="quantidade">Quantidade</Label>
              <Input
                id="quantidade"
                type="number"
                min={1}
                value={quantidade}
                onChange={(event) => setQuantidade(event.target.value)}
                required
              />
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="data">Data</Label>
              <Input
                id="data"
                type="date"
                value={data}
                onChange={(event) => setData(event.target.value)}
                required
              />
            </div>

            <div className="sm:col-span-5">
              <Button type="submit" disabled={lancar.isPending}>
                {lancar.isPending ? "Lançando…" : "Lançar movimentação"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Últimos lançamentos</CardTitle>
        </CardHeader>
        <CardContent>
          {movimentacoesQuery.isLoading ? (
            <p className="text-sm text-muted-foreground">Carregando…</p>
          ) : !movimentacoesQuery.data || movimentacoesQuery.data.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhuma movimentação registrada.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Data</TableHead>
                  <TableHead>Produto / Variação</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead className="text-right">Quantidade</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {movimentacoesQuery.data.slice(0, 20).map((mov) => (
                  <TableRow key={mov.id}>
                    <TableCell>{mov.data}</TableCell>
                    <TableCell>{variacoesPorId.get(mov.variacaoId) ?? mov.variacaoId}</TableCell>
                    <TableCell>{tipoLabels[mov.tipo]}</TableCell>
                    <TableCell className="text-right tabular-nums">{mov.quantidade}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
