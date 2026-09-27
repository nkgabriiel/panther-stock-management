"use client";

import { useMemo } from "react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { VariacaoCard } from "@/components/variacao-card";
import { useVariacoes } from "@/lib/queries";
import type { Variacao } from "@/lib/types";

function situacaoGeral(variacoes: Variacao[]) {
  const reduzir = variacoes
    .filter((v) => v.statusAnuncio === "REDUZIR")
    .sort((a, b) => a.folga - b.folga);

  if (reduzir.length > 0) {
    return {
      titulo: `Reduzir ${reduzir[0].nome.toLowerCase()}`,
      descricao: "Revise antes de liberar mais estoque",
      tom: "alerta" as const,
    };
  }

  const noLimite = variacoes.filter((v) => v.statusAnuncio === "NO_LIMITE");
  if (noLimite.length > 0) {
    return {
      titulo: `${noLimite[0].nome} no limite`,
      descricao: "Sem folga para anunciar mais",
      tom: "atencao" as const,
    };
  }

  return {
    titulo: "Tudo certo",
    descricao: "Pode aumentar os anúncios com segurança",
    tom: "ok" as const,
  };
}

export default function PainelPage() {
  const { data: variacoes, isLoading, isError } = useVariacoes();

  const resumo = useMemo(() => {
    const lista = variacoes ?? [];
    return {
      pecasProntas: lista.reduce((soma, v) => soma + v.pronto, 0),
      pedidosReservados: lista.reduce((soma, v) => soma + v.pedidosReservados, 0),
      pecasLivres: lista.reduce((soma, v) => soma + v.disponivelSeguro, 0),
      situacao: situacaoGeral(lista),
    };
  }, [variacoes]);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">
          Painel de estoque e limite de vendas
        </h1>
        <p className="text-sm text-muted-foreground">
          Atualize primeiro o estoque por cor e depois o estoque anunciado. Se aparecer REDUZIR,
          diminua os anúncios antes de vender novamente.
        </p>
      </div>

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Carregando…</p>
      ) : isError ? (
        <p className="text-sm text-destructive">Não foi possível carregar o estoque.</p>
      ) : !variacoes || variacoes.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            Nenhuma variação cadastrada ainda. Vá em{" "}
            <a href="/produtos" className="underline">
              Produtos e kits
            </a>{" "}
            para começar.
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <SummaryTile
              titulo="Peças prontas"
              valor={resumo.pecasProntas}
              descricao="Quantidade física concluída"
            />
            <SummaryTile
              titulo="Pedidos já reservados"
              valor={resumo.pedidosReservados}
              descricao="Vendidos, ainda não enviados"
            />
            <SummaryTile
              titulo="Peças livres com segurança"
              valor={resumo.pecasLivres}
              descricao="Saldo que sustenta os anúncios"
            />
            <Card
              className={
                resumo.situacao.tom === "alerta"
                  ? "bg-primary text-primary-foreground"
                  : undefined
              }
            >
              <CardHeader>
                <CardTitle className="text-xs font-normal opacity-80">Situação geral</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-lg font-semibold">{resumo.situacao.titulo}</p>
                <p className="text-xs opacity-80">{resumo.situacao.descricao}</p>
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {variacoes.map((variacao) => (
              <VariacaoCard key={variacao.id} variacao={variacao} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function SummaryTile({
  titulo,
  valor,
  descricao,
}: {
  titulo: string;
  valor: number;
  descricao: string;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xs font-normal text-muted-foreground">{titulo}</CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-semibold tabular-nums">{valor}</p>
        <p className="text-xs text-muted-foreground">{descricao}</p>
      </CardContent>
    </Card>
  );
}
