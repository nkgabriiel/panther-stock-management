const { execFileSync } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const frontendDir = path.join(__dirname, "..", "..", "frontend", "panther-stock-management-frontend");
const destDir = path.join(__dirname, "..", "resources", "frontend");

console.log("Building frontend (standalone output)...");
const npmCmd = process.platform === "win32" ? "npm.cmd" : "npm";
execFileSync(npmCmd, ["run", "build"], {
  cwd: frontendDir,
  stdio: "inherit",
  shell: process.platform === "win32",
});

function copyDir(src, dest) {
  fs.rmSync(dest, { recursive: true, force: true });
  fs.cpSync(src, dest, { recursive: true });
}

const standaloneDir = path.join(frontendDir, ".next", "standalone");
if (!fs.existsSync(standaloneDir)) {
  throw new Error("Standalone build not found at " + standaloneDir + " - is output:'standalone' set in next.config.ts?");
}

copyDir(standaloneDir, destDir);
copyDir(path.join(frontendDir, ".next", "static"), path.join(destDir, ".next", "static"));

const publicDir = path.join(frontendDir, "public");
if (fs.existsSync(publicDir)) {
  copyDir(publicDir, path.join(destDir, "public"));
}

console.log("Copied frontend standalone build -> resources/frontend");
