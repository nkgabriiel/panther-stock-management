"use client";

import { useMemo, useState } from "react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCriarProduto, useCriarVariacao, useProdutos, useVariacoes } from "@/lib/queries";
import type { TipoProduto, Variacao } from "@/lib/types";
import { ApiError } from "@/lib/api-client";

export default function ProdutosPage() {
  const produtosQuery = useProdutos();
  const variacoesQuery = useVariacoes();

  const variacoes = variacoesQuery.data;

  const variacoesPorProduto = useMemo(() => {
    const mapa = new Map<number, Variacao[]>();
    for (const variacao of variacoes ?? []) {
      const lista = mapa.get(variacao.produtoId) ?? [];
      lista.push(variacao);
      mapa.set(variacao.produtoId, lista);
    }
    return mapa;
  }, [variacoes]);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold tracking-tight">Produtos e kits</h1>
          <p className="text-sm text-muted-foreground">
            Cadastre produtos e suas variações (cores, combinações de kit).
          </p>
        </div>
        <NovoProdutoDialog />
      </div>

      {produtosQuery.isLoading ? (
        <p className="text-sm text-muted-foreground">Carregando…</p>
      ) : produtosQuery.isError ? (
        <p className="text-sm text-destructive">Não foi possível carregar os produtos.</p>
      ) : produtosQuery.data && produtosQuery.data.length > 0 ? (
        <div className="flex flex-col gap-4">
          {produtosQuery.data.map((produto) => (
            <Card key={produto.id}>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  {produto.nome}
                  <Badge variant="secondary">{produto.tipo}</Badge>
                </CardTitle>
                <CardDescription>
                  {(variacoesPorProduto.get(produto.id) ?? []).length} variação(ões)
                </CardDescription>
                <CardAction>
                  <NovaVariacaoDialog produtoId={produto.id} produtoNome={produto.nome} />
                </CardAction>
              </CardHeader>
              <CardContent>
                {(variacoesPorProduto.get(produto.id) ?? []).length === 0 ? (
                  <p className="text-sm text-muted-foreground">
                    Nenhuma variação cadastrada ainda.
                  </p>
                ) : (
                  <div className="flex flex-wrap gap-2">
                    {(variacoesPorProduto.get(produto.id) ?? []).map((variacao) => (
                      <Badge key={variacao.id} variant="outline" className="text-sm">
                        {variacao.nome} · reserva {variacao.reservaSeguranca}
                      </Badge>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            Nenhum produto cadastrado ainda. Comece criando um produto acima.
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function NovoProdutoDialog() {
  const [open, setOpen] = useState(false);
  const [nome, setNome] = useState("");
  const [tipo, setTipo] = useState<TipoProduto>("UNIDADE");
  const criarProduto = useCriarProduto();

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    try {
      await criarProduto.mutateAsync({ nome, tipo });
      toast.success(`Produto "${nome}" criado`);
      setNome("");
      setTipo("UNIDADE");
      setOpen(false);
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao criar produto");
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger render={<Button />}>Novo produto</DialogTrigger>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Novo produto</DialogTitle>
            <DialogDescription>
              Ex: &quot;Touca unidade&quot; ou &quot;Kit 3 toucas&quot;.
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-4 py-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="nome">Nome</Label>
              <Input
                id="nome"
                value={nome}
                onChange={(event) => setNome(event.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="tipo">Tipo</Label>
              <Select value={tipo} onValueChange={(value) => setTipo(value as TipoProduto)}>
                <SelectTrigger id="tipo">
                  <SelectValue>
                    {(value: TipoProduto | null) => (value === "KIT" ? "Kit" : "Unidade")}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="UNIDADE">Unidade</SelectItem>
                  <SelectItem value="KIT">Kit</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button type="submit" disabled={criarProduto.isPending}>
              {criarProduto.isPending ? "Salvando…" : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function NovaVariacaoDialog({
  produtoId,
  produtoNome,
}: {
  produtoId: number;
  produtoNome: string;
}) {
  const [open, setOpen] = useState(false);
  const [nome, setNome] = useState("");
  const [reservaSeguranca, setReservaSeguranca] = useState("0");
  const criarVariacao = useCriarVariacao();

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    try {
      await criarVariacao.mutateAsync({
        produtoId,
        nome,
        reservaSeguranca: Number(reservaSeguranca),
      });
      toast.success(`Variação "${nome}" criada`);
      setNome("");
      setReservaSeguranca("0");
      setOpen(false);
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao criar variação");
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger render={<Button variant="outline" size="sm" />}>
        + Variação
      </DialogTrigger>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Nova variação em {produtoNome}</DialogTitle>
            <DialogDescription>
              Ex: cor (&quot;Preto&quot;) ou combinação (&quot;2 Pretas + 1 Branca&quot;).
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-4 py-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="variacao-nome">Nome</Label>
              <Input
                id="variacao-nome"
                value={nome}
                onChange={(event) => setNome(event.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="reserva">Reserva de segurança</Label>
              <Input
                id="reserva"
                type="number"
                min={0}
                value={reservaSeguranca}
                onChange={(event) => setReservaSeguranca(event.target.value)}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="submit" disabled={criarVariacao.isPending}>
              {criarVariacao.isPending ? "Salvando…" : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
