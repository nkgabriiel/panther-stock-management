const { execFileSync } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const backendDir = path.join(__dirname, "..", "..", "backend");
const resourcesDir = path.join(__dirname, "..", "resources");

console.log("Building backend jar...");
const mvnw = path.join(backendDir, process.platform === "win32" ? "mvnw.cmd" : "mvnw");
execFileSync(mvnw, ["-q", "-DskipTests", "package"], {
  cwd: backendDir,
  stdio: "inherit",
  shell: process.platform === "win32",
});

const targetDir = path.join(backendDir, "target");
const jar = fs
  .readdirSync(targetDir)
  .find((name) => name.endsWith(".jar") && !name.endsWith("-sources.jar") && !name.endsWith(".original"));

if (!jar) {
  throw new Error("Could not find built backend jar in " + targetDir);
}

fs.mkdirSync(resourcesDir, { recursive: true });
fs.copyFileSync(path.join(targetDir, jar), path.join(resourcesDir, "backend.jar"));
console.log("Copied " + jar + " -> resources/backend.jar");
