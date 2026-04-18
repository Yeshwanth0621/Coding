import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const distDir = path.join(__dirname, 'dist');
const homeDir = path.join(distDir, 'home');

if (!fs.existsSync(distDir)) {
    console.error('Dist directory not found!');
    process.exit(1);
}

// Create dist/home
if (!fs.existsSync(homeDir)) {
    fs.mkdirSync(homeDir, { recursive: true });
}

// Move all files from dist to dist/home (except home itself)
const files = fs.readdirSync(distDir);
files.forEach(file => {
    if (file === 'home') return;

    const oldPath = path.join(distDir, file);
    const newPath = path.join(homeDir, file);

    fs.renameSync(oldPath, newPath);
});

// Create a root redirect index.html
const redirectHtml = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Redirecting...</title>
  <script>
    window.location.href = "/kkc/home/";
  </script>
  <meta http-equiv="refresh" content="0; url=/kkc/home/">
</head>
<body>
  Redirecting to <a href="/kkc/home/">/kkc/home/</a>...
</body>
</html>
`;

fs.writeFileSync(path.join(distDir, 'index.html'), redirectHtml);

// SPA Hack for GitHub Pages: Copy index.html to 404.html in the app directory
fs.copyFileSync(path.join(homeDir, 'index.html'), path.join(homeDir, '404.html'));

console.log('Build rearranged for /kkc/home/ deployment with root redirect and SPA support.');
