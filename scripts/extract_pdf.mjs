import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const pdfPath = process.argv[2] || path.join(__dirname, '..', '..', '..', 'Downloads', 'horarios_semestre5.pdf');

console.log('Buscando PDF en:', pdfPath);

if (!fs.existsSync(pdfPath)) {
  console.error('❌ No se encontro el PDF en la ruta especificada.');
  process.exit(1);
}

try {
  let pdfParse;
  try {
    pdfParse = (await import('pdf-parse')).default;
  } catch (e) {
    console.log('⚠️  pdf-parse no instalado. Instalando temporalmente...');
    const { execSync } = await import('child_process');
    execSync('npm install pdf-parse --silent --no-save', { stdio: 'inherit', cwd: __dirname });
    pdfParse = (await import('pdf-parse')).default;
  }

  const dataBuffer = fs.readFileSync(pdfPath);
  const data = await pdfParse(dataBuffer);

  console.log('\n======== INICIO DEL CONTENIDO DEL PDF ========\n');
  console.log(data.text);
  console.log('\n======== FIN DEL CONTENIDO DEL PDF ========\n');

  console.log('📄 Paginas:', data.numpages);
  console.log('ℹ️  Info:', JSON.stringify(data.info, null, 2));
} catch (err) {
  console.error('❌ Error al extraer el texto:', err.message);
  console.error(err.stack);
}
