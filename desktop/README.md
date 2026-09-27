# Argoni Stock Management — desktop

Empacota o backend (Spring Boot + Postgres embutido) e o frontend (Next.js) num
único aplicativo Electron para Windows. Sem Docker, sem instalar Java ou
Postgres separadamente — tudo roda a partir do instalador.

## Como funciona

`main.js` sobe dois processos filhos e gerencia o ciclo de vida deles:

1. **Backend**: `resources/jre/bin/java.exe -jar resources/backend.jar` com
   `APP_EMBEDDED_POSTGRES=true`. O backend sobe seu próprio Postgres (via
   `embedded-postgres`) num diretório persistente em `%USERPROFILE%\ArgoniStock\pgdata`.
2. **Frontend**: o build `standalone` do Next.js, rodado com o próprio binário
   do Electron atuando como Node (`ELECTRON_RUN_AS_NODE=1`) — não precisa
   embutir um Node.js separado.

Uma API key aleatória é gerada a cada início e passada pros dois processos via
variável de ambiente, então nunca fica salva em disco.

Ao fechar a janela, o app chama `POST /internal/shutdown` no backend (em vez
de matar o processo à força) para que o Postgres embutido feche de forma
limpa — no Windows, matar o processo Java diretamente deixaria o Postgres
órfão.

## Build

Da primeira vez, ou sempre que mudar o código do backend/frontend:

```bash
npm install
npm run dist
```

Isso builda o `.jar` do backend, o build `standalone` do frontend, baixa (ou
reusa do cache) um JRE 21 da Temurin, e gera o instalador em `release/`.

Rodar sem empacotar (usa os `resources/` já preparados):

```bash
npm start
```

## Nota sobre o build no Windows sem modo desenvolvedor

O `electron-builder` baixa um pacote chamado `winCodeSign` que contém, entre
outras coisas, binários de assinatura de código do macOS — e esse pacote
inclui symlinks Unix que o Windows recusa a criar sem "Developer Mode" ou
privilégio de administrador, mesmo quando não vamos assinar nada. Se o build
falhar com `Cannot create symbolic link`, é isso.

Não precisamos desses arquivos (são só para assinar builds de macOS), então
o workaround é extrair o pacote manualmente ignorando a pasta `darwin`:

```powershell
Remove-Item -Recurse -Force "$env:LOCALAPPDATA\electron-builder\Cache\winCodeSign\winCodeSign-2.6.0" -ErrorAction SilentlyContinue
& "node_modules\7zip-bin\win\x64\7za.exe" x -bd "$env:LOCALAPPDATA\electron-builder\Cache\winCodeSign\<algum-arquivo-numerado>.7z" -o"$env:LOCALAPPDATA\electron-builder\Cache\winCodeSign\winCodeSign-2.6.0" -xr!darwin
```

(o `<algum-arquivo-numerado>.7z` aparece na pasta depois da primeira tentativa
de build falhar — é o download que o electron-builder já fez, só não
conseguiu extrair.) Depois disso `npm run dist` funciona normalmente, porque
o electron-builder encontra a pasta já extraída e não tenta baixar de novo.

Alternativa mais simples se a máquina permitir: ativar o "Developer Mode" do
Windows (Configurações → Privacidade e segurança → Para desenvolvedores),
que permite criar symlinks sem admin.

## Atualização automática

O app usa `electron-updater`, que usa o GitHub Releases deste repositório como
"feed" de atualizações (config `build.publish` no `package.json`). Toda vez
que o app inicia (build empacotado, não em `npm start`), ele checa
silenciosamente se há uma versão mais nova publicada, baixa em segundo plano
e, quando termina, mostra um diálogo perguntando se a pessoa quer reiniciar
na hora ou deixar para a próxima vez que fechar o app.

Isso só funciona a partir da primeira versão publicada com esse mecanismo
(esta). Instaladores anteriores a essa versão continuam sem atualização
automática — quem já tiver uma versão anterior instalada precisa baixar e
instalar manualmente essa vez; a partir daqui, as próximas atualizações são
automáticas.

**Publicar uma nova versão**: além do instalador (`.exe`), o
`electron-builder` também gera um `.exe.blockmap` e um `latest.yml` dentro de
`release/`. Os **três arquivos** precisam ser anexados à Release do GitHub
(não só o `.exe`) — é o `latest.yml` que o `electron-updater` consulta para
saber se há uma versão nova e onde baixar.

## Configuração de backup

O backend expõe endpoints (`/api/backup/*`) para configurar a pasta local e a
pasta do Google Drive onde os backups em Excel são salvos — isso é feito pela
própria aba "Backup" do app, não precisa mexer em nada aqui.
