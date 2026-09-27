"use client";

import { useMemo, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { cn } from "@/lib/utils";
import { useMovimentacoes, useProdutos, useVariacoes } from "@/lib/queries";

const periodos = [
  { label: "7 dias", dias: 7 },
  { label: "14 dias", dias: 14 },
  { label: "30 dias", dias: 30 },
];

function formatarData(diasAtras: number) {
  const data = new Date();
  data.setDate(data.getDate() - diasAtras);
  return data.toISOString().slice(0, 10);
}

export default function HistoricoPage() {
  const [dias, setDias] = useState(14);
  const [produtoId, setProdutoId] = useState<string>("todos");

  const produtosQuery = useProdutos();
  const variacoesQuery = useVariacoes();

  const dataInicio = formatarData(dias - 1);
  const dataFim = formatarData(0);

  const movimentacoesQuery = useMovimentacoes({
    dataInicio,
    dataFim,
    produtoId: produtoId === "todos" ? undefined : Number(produtoId),
  });

  const variacoesPorId = useMemo(() => {
    const produtosPorId = new Map((produtosQuery.data ?? []).map((p) => [p.id, p.nome]));
    return new Map(
      (variacoesQuery.data ?? []).map((v) => [
        v.id,
        `${produtosPorId.get(v.produtoId) ?? "?"} · ${v.nome}`,
      ]),
    );
  }, [variacoesQuery.data, produtosQuery.data]);

  const movimentacoes = useMemo(() => movimentacoesQuery.data ?? [], [movimentacoesQuery.data]);

  const resumo = useMemo(() => {
    let vendas = 0;
    let producao = 0;
    let ajustes = 0;
    for (const mov of movimentacoes) {
      if (mov.tipo === "VENDA") vendas += mov.quantidade;
      if (mov.tipo === "PRODUCAO_RECEBIDA") producao += mov.quantidade;
      if (mov.tipo === "AJUSTE") ajustes += mov.quantidade;
    }
    return { vendas, producao, ajustes };
  }, [movimentacoes]);

  const dadosGrafico = useMemo(() => {
    const porDia = new Map<string, { data: string; vendas: number; producao: number }>();
    for (const mov of movimentacoes) {
      const entrada = porDia.get(mov.data) ?? { data: mov.data, vendas: 0, producao: 0 };
      if (mov.tipo === "VENDA") entrada.vendas += mov.quantidade;
      if (mov.tipo === "PRODUCAO_RECEBIDA") entrada.producao += mov.quantidade;
      porDia.set(mov.data, entrada);
    }
    return Array.from(porDia.values()).sort((a, b) => a.data.localeCompare(b.data));
  }, [movimentacoes]);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">Histórico e relatório</h1>
        <p className="text-sm text-muted-foreground">
          Movimentação de estoque por dia, produto e cor.
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {periodos.map((periodo) => (
          <Button
            key={periodo.dias}
            size="sm"
            variant={dias === periodo.dias ? "default" : "outline"}
            onClick={() => setDias(periodo.dias)}
          >
            {periodo.label}
          </Button>
        ))}
        <Select value={produtoId} onValueChange={(value) => setProdutoId(value ?? "todos")}>
          <SelectTrigger className="w-48">
            <SelectValue>
              {(value: string | null) =>
                !value || value === "todos"
                  ? "Todos os produtos"
                  : (produtosQuery.data ?? []).find((p) => String(p.id) === value)?.nome
              }
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="todos">Todos os produtos</SelectItem>
            {(produtosQuery.data ?? []).map((produto) => (
              <SelectItem key={produto.id} value={String(produto.id)}>
                {produto.nome}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <SummaryTile titulo="Vendas no período" valor={resumo.vendas} />
        <SummaryTile titulo="Produção recebida" valor={resumo.producao} />
        <SummaryTile titulo="Ajustes" valor={resumo.ajustes} sinal />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Vendas e produção — últimos {dias} dias</CardTitle>
        </CardHeader>
        <CardContent>
          {dadosGrafico.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sem movimentações no período.</p>
          ) : (
            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={dadosGrafico}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                  <XAxis
                    dataKey="data"
                    tickFormatter={(value: string) => value.slice(8, 10)}
                    fontSize={12}
                  />
                  <YAxis fontSize={12} allowDecimals={false} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="vendas" name="Vendas" fill="var(--chart-1)" radius={[4, 4, 0, 0]} />
                  <Bar
                    dataKey="producao"
                    name="Produção"
                    fill="var(--chart-2)"
                    radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Movimentações</CardTitle>
        </CardHeader>
        <CardContent>
          {movimentacoesQuery.isLoading ? (
            <p className="text-sm text-muted-foreground">Carregando…</p>
          ) : movimentacoes.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhuma movimentação no período.</p>
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
                {movimentacoes.map((mov) => (
                  <TableRow key={mov.id}>
                    <TableCell>{mov.data}</TableCell>
                    <TableCell>{variacoesPorId.get(mov.variacaoId) ?? mov.variacaoId}</TableCell>
                    <TableCell>{mov.tipo}</TableCell>
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

function SummaryTile({
  titulo,
  valor,
  sinal = false,
}: {
  titulo: string;
  valor: number;
  sinal?: boolean;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xs font-normal text-muted-foreground">{titulo}</CardTitle>
      </CardHeader>
      <CardContent>
        <p
          className={cn(
            "text-2xl font-semibold tabular-nums",
            sinal && valor < 0 && "text-destructive",
          )}
        >
          {sinal && valor > 0 ? `+${valor}` : valor}
        </p>
      </CardContent>
    </Card>
  );
}
