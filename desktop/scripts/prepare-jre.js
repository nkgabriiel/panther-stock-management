const { execFileSync } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const cacheDir = path.join(__dirname, "..", ".cache");
const zipPath = path.join(cacheDir, "jre-windows-x64.zip");
const jreDest = path.join(__dirname, "..", "resources", "jre");

const ADOPTIUM_API =
  "https://api.adoptium.net/v3/assets/latest/21/hotspot?image_type=jre&os=windows&architecture=x64";

async function ensureZipDownloaded() {
  fs.mkdirSync(cacheDir, { recursive: true });
  if (fs.existsSync(zipPath) && fs.statSync(zipPath).size > 1_000_000) {
    console.log("Using cached JRE zip at " + zipPath);
    return;
  }

  console.log("Looking up latest Temurin 21 JRE for Windows x64...");
  const info = await fetch(ADOPTIUM_API).then((res) => res.json());
  const url = info[0].binary.package.link;
  console.log("Downloading " + url);

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error("Failed to download JRE: " + response.status);
  }
  const buffer = Buffer.from(await response.arrayBuffer());
  fs.writeFileSync(zipPath, buffer);
  console.log("Downloaded " + buffer.length + " bytes");
}

function extractZip() {
  const extractTmp = path.join(cacheDir, "jre-extract-tmp");
  fs.rmSync(extractTmp, { recursive: true, force: true });
  fs.mkdirSync(extractTmp, { recursive: true });

  console.log("Extracting JRE zip...");
  execFileSync(
    "powershell",
    ["-NoProfile", "-Command", `Expand-Archive -Path "${zipPath}" -DestinationPath "${extractTmp}" -Force`],
    { stdio: "inherit" },
  );

  const entries = fs.readdirSync(extractTmp);
  const jreFolder = entries.find((name) => fs.statSync(path.join(extractTmp, name)).isDirectory());
  if (!jreFolder) {
    throw new Error("Could not find extracted JRE folder inside " + extractTmp);
  }

  fs.rmSync(jreDest, { recursive: true, force: true });
  fs.renameSync(path.join(extractTmp, jreFolder), jreDest);
  fs.rmSync(extractTmp, { recursive: true, force: true });
  console.log("JRE ready at resources/jre");
}

(async () => {
  if (fs.existsSync(path.join(jreDest, "bin", "java.exe"))) {
    console.log("resources/jre already present, skipping download/extract.");
    return;
  }
  await ensureZipDownloaded();
  extractZip();
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
