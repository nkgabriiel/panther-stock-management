const { app, BrowserWindow, dialog } = require("electron");
const { autoUpdater } = require("electron-updater");
const { spawn } = require("node:child_process");
const crypto = require("node:crypto");
const fs = require("node:fs");
const path = require("node:path");

const isPackaged = app.isPackaged;
const resourcesDir = isPackaged ? process.resourcesPath : path.join(__dirname, "resources");

const BACKEND_PORT = 8080;
const FRONTEND_PORT = 3000;
const API_KEY = crypto.randomBytes(24).toString("hex");

const logDir = path.join(app.getPath("userData"), "logs");
fs.mkdirSync(logDir, { recursive: true });
const backendLog = fs.createWriteStream(path.join(logDir, "backend.log"), { flags: "a" });
const frontendLog = fs.createWriteStream(path.join(logDir, "frontend.log"), { flags: "a" });

let backendProcess = null;
let frontendProcess = null;
let mainWindow = null;
let loadingWindow = null;
let quitting = false;

function javaExecutable() {
  const bundled = path.join(resourcesDir, "jre", "bin", "java.exe");
  if (fs.existsSync(bundled)) {
    return bundled;
  }
  // Fallback for local dev runs before `npm run prepare:jre` has been executed.
  return "java";
}

function startBackend() {
  const jar = path.join(resourcesDir, "backend.jar");
  const java = javaExecutable();

  backendProcess = spawn(java, ["-jar", jar], {
    env: {
      ...process.env,
      APP_EMBEDDED_POSTGRES: "true",
      API_KEY,
      SERVER_PORT: String(BACKEND_PORT),
    },
    cwd: resourcesDir,
  });

  backendProcess.stdout.on("data", (chunk) => backendLog.write(chunk));
  backendProcess.stderr.on("data", (chunk) => backendLog.write(chunk));
  backendProcess.on("exit", (code) => {
    backendLog.write(`\n[backend exited with code ${code}]\n`);
    backendProcess = null;
    if (!quitting) {
      // The backend died unexpectedly; there's nothing useful left to show.
      app.quit();
    }
  });
}

function startFrontend() {
  const frontendDir = path.join(resourcesDir, "frontend");
  const serverScript = path.join(frontendDir, "server.js");

  frontendProcess = spawn(process.execPath, [serverScript], {
    env: {
      ...process.env,
      ELECTRON_RUN_AS_NODE: "1",
      PORT: String(FRONTEND_PORT),
      HOSTNAME: "127.0.0.1",
      BACKEND_URL: `http://127.0.0.1:${BACKEND_PORT}`,
      BACKEND_API_KEY: API_KEY,
    },
    cwd: frontendDir,
  });

  frontendProcess.stdout.on("data", (chunk) => frontendLog.write(chunk));
  frontendProcess.stderr.on("data", (chunk) => frontendLog.write(chunk));
  frontendProcess.on("exit", (code) => {
    frontendLog.write(`\n[frontend exited with code ${code}]\n`);
    frontendProcess = null;
    if (!quitting) {
      app.quit();
    }
  });
}

async function waitForHttpOk(url, timeoutMs) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    try {
      const response = await fetch(url, { signal: AbortSignal.timeout(2000) });
      if (response.ok) {
        return true;
      }
    } catch {
      // still starting up, keep polling
    }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }
  return false;
}

function createLoadingWindow() {
  loadingWindow = new BrowserWindow({
    width: 420,
    height: 280,
    resizable: false,
    frame: false,
    icon: path.join(__dirname, "build", "icon.png"),
  });

  const html = `<!doctype html>
<html>
<head><meta charset="utf-8"></head>
<body style="margin:0;height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;
  font-family:Segoe UI, sans-serif;background:#faf6f2;color:#3a2a30;gap:16px;">
  <div style="font-size:20px;font-weight:600;letter-spacing:0.05em;">ARGONI</div>
  <div style="font-size:13px;color:#7a6a70;">Iniciando o sistema, um momento...</div>
</body>
</html>`;
  loadingWindow.loadURL("data:text/html," + encodeURIComponent(html));
}

function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    show: false,
    icon: path.join(__dirname, "build", "icon.png"),
    title: "Argoni Stock Management",
  });

  mainWindow.once("ready-to-show", () => {
    if (loadingWindow) {
      loadingWindow.close();
      loadingWindow = null;
    }
    mainWindow.show();
  });

  mainWindow.loadURL(`http://127.0.0.1:${FRONTEND_PORT}`);
}

function setupAutoUpdater() {
  if (!isPackaged) return;

  autoUpdater.autoDownload = true;
  autoUpdater.autoInstallOnAppQuit = true;

  const log = (message) => backendLog.write(`[updater] ${message}\n`);
  autoUpdater.logger = { info: log, warn: log, error: log, debug: () => {} };

  autoUpdater.on("update-downloaded", (info) => {
    dialog
      .showMessageBox(mainWindow, {
        type: "info",
        buttons: ["Reiniciar agora", "Depois"],
        defaultId: 0,
        cancelId: 1,
        title: "Atualização disponível",
        message: `Uma nova versão (${info.version}) foi baixada.`,
        detail: "Reinicie o aplicativo agora para aplicar a atualização, ou ela será aplicada automaticamente na próxima vez que você fechar o aplicativo.",
      })
      .then(({ response }) => {
        if (response === 0) {
          quitting = true;
          autoUpdater.quitAndInstall();
        }
      });
  });

  autoUpdater.on("error", (error) => {
    log(`error: ${error == null ? error : error.stack || error.message || error}`);
  });

  autoUpdater.checkForUpdates().catch((error) => {
    log(`checkForUpdates failed: ${error == null ? error : error.message || error}`);
  });
}

async function boot() {
  createLoadingWindow();

  startBackend();
  const backendReady = await waitForHttpOk(`http://127.0.0.1:${BACKEND_PORT}/health`, 60_000);
  if (!backendReady) {
    backendLog.write("\n[timed out waiting for backend to become healthy]\n");
    app.quit();
    return;
  }

  startFrontend();
  const frontendReady = await waitForHttpOk(`http://127.0.0.1:${FRONTEND_PORT}`, 30_000);
  if (!frontendReady) {
    frontendLog.write("\n[timed out waiting for frontend to become ready]\n");
    app.quit();
    return;
  }

  createMainWindow();
  setupAutoUpdater();
}

async function shutdownBackendGracefully() {
  if (!backendProcess) return;
  try {
    await fetch(`http://127.0.0.1:${BACKEND_PORT}/internal/shutdown`, {
      method: "POST",
      headers: { "X-API-Key": API_KEY },
      signal: AbortSignal.timeout(2000),
    });
  } catch {
    // backend may already be gone; fall through to the forced kill below
  }
}

app.whenReady().then(boot);

app.on("window-all-closed", () => {
  app.quit();
});

app.on("before-quit", (event) => {
  if (quitting) return;
  quitting = true;

  if (backendProcess || frontendProcess) {
    event.preventDefault();
    (async () => {
      await shutdownBackendGracefully();
      await new Promise((resolve) => setTimeout(resolve, 1000));
      if (backendProcess) backendProcess.kill();
      if (frontendProcess) frontendProcess.kill();
      app.quit();
    })();
  }
});
