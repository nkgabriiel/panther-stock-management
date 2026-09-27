"use client";

import { useRef, useState } from "react";
import { toast } from "sonner";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { api, ApiError } from "@/lib/api-client";
import type { ConfiguracaoBackup } from "@/lib/types";
import {
  useAtualizarConfiguracaoBackup,
  useConfiguracaoBackup,
  useGerarBackup,
  useImportarBackup,
} from "@/lib/queries";

export default function BackupPage() {
  const configuracaoQuery = useConfiguracaoBackup();
  const importarBackup = useImportarBackup();

  const [arquivoEscolhido, setArquivoEscolhido] = useState<File | null>(null);
  const [confirmarImportacao, setConfirmarImportacao] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  function escolherArquivo(event: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = event.target.files?.[0] ?? null;
    setArquivoEscolhido(arquivo);
    if (arquivo) {
      setConfirmarImportacao(true);
    }
  }

  async function confirmarEImportar() {
    if (!arquivoEscolhido) return;
    try {
      await importarBackup.mutateAsync(arquivoEscolhido);
      toast.success("Dados importados com sucesso");
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao importar planilha");
    } finally {
      setConfirmarImportacao(false);
      setArquivoEscolhido(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  function cancelarImportacao() {
    setConfirmarImportacao(false);
    setArquivoEscolhido(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">Backup e restauração</h1>
        <p className="text-sm text-muted-foreground">
          Gere uma cópia dos dados em Excel, salva no seu computador e (opcionalmente) numa pasta
          do Google Drive sincronizada. Importe uma cópia salva para restaurar os dados.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Configuração de backup</CardTitle>
          <CardDescription>
            Deixe a pasta do Drive em branco se não quiser uma segunda cópia sincronizada.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {configuracaoQuery.isLoading ? (
            <p className="text-sm text-muted-foreground">Carregando…</p>
          ) : (
            <ConfiguracaoBackupForm configuracao={configuracaoQuery.data ?? null} />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Importar planilha</CardTitle>
          <CardDescription>
            Aceita apenas um arquivo gerado por este próprio sistema (backup local ou do Drive).
            Isso substitui todos os dados atuais.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Input
            ref={fileInputRef}
            type="file"
            accept=".xlsx"
            onChange={escolherArquivo}
            disabled={importarBackup.isPending}
          />
        </CardContent>
      </Card>

      <Dialog
        open={confirmarImportacao}
        onOpenChange={(open) => {
          if (!open) cancelarImportacao();
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Substituir todos os dados?</DialogTitle>
            <DialogDescription>
              Importar &quot;{arquivoEscolhido?.name}&quot; vai apagar os dados atuais e
              substituir por tudo que está na planilha. Essa ação não pode ser desfeita. Tem
              certeza?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={cancelarImportacao}>
              Cancelar
            </Button>
            <Button onClick={confirmarEImportar} disabled={importarBackup.isPending}>
              {importarBackup.isPending ? "Importando…" : "Sim, substituir dados"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function ConfiguracaoBackupForm({ configuracao }: { configuracao: ConfiguracaoBackup | null }) {
  const [pastaLocal, setPastaLocal] = useState(configuracao?.pastaLocal ?? "");
  const [pastaDrive, setPastaDrive] = useState(configuracao?.pastaDrive ?? "");
  const atualizarConfiguracao = useAtualizarConfiguracaoBackup();
  const gerarBackup = useGerarBackup();

  async function salvarConfiguracao(event: React.FormEvent) {
    event.preventDefault();
    try {
      await atualizarConfiguracao.mutateAsync({ pastaLocal, pastaDrive });
      toast.success("Configuração de backup salva");
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao salvar configuração");
    }
  }

  async function fazerBackupAgora() {
    try {
      const resultado = await gerarBackup.mutateAsync();
      toast.success(`Backup salvo em ${resultado.arquivosGravados.length} local(is)`, {
        description: resultado.arquivosGravados.join("\n"),
      });
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Erro ao gerar backup");
    }
  }

  return (
    <form onSubmit={salvarConfiguracao} className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        <Label htmlFor="pastaLocal">Pasta local (no seu computador)</Label>
        <Input
          id="pastaLocal"
          placeholder="Ex: C:\Users\seu-usuario\PantherEstoque\backups"
          value={pastaLocal}
          onChange={(event) => setPastaLocal(event.target.value)}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Label htmlFor="pastaDrive">Pasta do Google Drive (sincronizada)</Label>
        <Input
          id="pastaDrive"
          placeholder="Ex: C:\Users\seu-usuario\Google Drive\PantherEstoque"
          value={pastaDrive}
          onChange={(event) => setPastaDrive(event.target.value)}
        />
      </div>
      {configuracao?.ultimoBackupEm && (
        <p className="text-xs text-muted-foreground">
          Último backup em {new Date(configuracao.ultimoBackupEm).toLocaleString("pt-BR")}
        </p>
      )}
      <div className="flex flex-wrap gap-2">
        <Button type="submit" disabled={atualizarConfiguracao.isPending}>
          {atualizarConfiguracao.isPending ? "Salvando…" : "Salvar configuração"}
        </Button>
        <Button
          type="button"
          variant="outline"
          onClick={fazerBackupAgora}
          disabled={gerarBackup.isPending}
        >
          {gerarBackup.isPending ? "Gerando…" : "Fazer backup agora"}
        </Button>
        <a href={api.backup.urlDownload()} download className={buttonVariants({ variant: "ghost" })}>
          Baixar backup agora
        </a>
      </div>
    </form>
  );
}
