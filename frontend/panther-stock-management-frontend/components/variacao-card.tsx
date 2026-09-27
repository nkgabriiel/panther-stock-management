"use client";

import { useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Progress } from "@/components/ui/progress";
import { StatusBadge } from "@/components/status-badge";
import { useAtualizarEstoqueAnunciado } from "@/lib/queries";
import type { Variacao } from "@/lib/types";
import { ApiError } from "@/lib/api-client";

export function VariacaoCard({ variacao }: { variacao: Variacao }) {
  const [valor, setValor] = useState(String(variacao.estoqueAnunciado));
  const atualizar = useAtualizarEstoqueAnunciado();

  const progresso =
    variacao.disponivelSeguro > 0
      ? Math.min(100, (variacao.estoqueAnunciado / variacao.disponivelSeguro) * 100)
      : variacao.estoqueAnunciado > 0
        ? 100
        : 0;

  async function salvar() {
    const numero = Number(valor);
    if (!Number.isFinite(numero) || numero < 0) {
      toast.error("Informe um número válido");
      return;
    }
    try {
      await atualizar.mutateAsync({ id: variacao.id, estoqueAnunciado: numero });
      toast.success(`Estoque anunciado de "${variacao.nome}" atualizado`);
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao atualizar");
    }
  }

  return (
    <Card
      className={
        variacao.statusAnuncio === "REDUZIR" ? "ring-1 ring-amber-300 dark:ring-amber-800" : ""
      }
    >
      <CardHeader>
        <CardTitle className="flex items-center justify-between gap-2">
          <span>{variacao.nome}</span>
          <StatusBadge status={variacao.statusAnuncio} folga={variacao.folga} />
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <div>
          <div className="text-3xl font-semibold tabular-nums">{variacao.disponivelSeguro}</div>
          <p className="text-xs text-muted-foreground">disponível seguro</p>
        </div>

        <div className="flex flex-col gap-1.5">
          <Progress value={progresso} />
          <div className="flex justify-between text-xs text-muted-foreground">
            <span>{variacao.estoqueAnunciado} nos anúncios</span>
            <span>
              {variacao.folga >= 0 ? `+${variacao.folga} de folga` : `${Math.abs(variacao.folga)} acima do seguro`}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
          <span className="text-muted-foreground">Pronto</span>
          <span className="text-right font-medium tabular-nums">{variacao.pronto}</span>
          <span className="text-muted-foreground">Produção garantida</span>
          <span className="text-right font-medium tabular-nums">{variacao.producaoGarantida}</span>
          <span className="text-muted-foreground">Pedidos reservados</span>
          <span className="text-right font-medium tabular-nums">{variacao.pedidosReservados}</span>
          <span className="text-muted-foreground">Reserva</span>
          <span className="text-right font-medium tabular-nums">{variacao.reservaSeguranca}</span>
        </div>

        <div className="flex items-end gap-2 border-t pt-3">
          <div className="flex-1">
            <label className="text-xs text-muted-foreground" htmlFor={`anunciado-${variacao.id}`}>
              Atualizar estoque anunciado
            </label>
            <Input
              id={`anunciado-${variacao.id}`}
              type="number"
              min={0}
              value={valor}
              onChange={(event) => setValor(event.target.value)}
              className="mt-1"
            />
          </div>
          <Button size="sm" onClick={salvar} disabled={atualizar.isPending}>
            {atualizar.isPending ? "Salvando…" : "Salvar"}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
