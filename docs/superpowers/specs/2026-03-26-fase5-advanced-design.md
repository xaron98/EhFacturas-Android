# EhFacturas Android — Spec Fase 5: Features Avanzadas

## Resumen

6 features avanzadas portadas 1:1 desde iOS, con mejora de WorkManager para recurrentes.

---

## 1. Importador CSV

### Flujo

1. Usuario selecciona archivo CSV (SAF ActivityResultContracts.GetContent)
2. Auto-detección: encoding (UTF-8/ISO-8859-1/Windows-1252), separador (;/,/\t), programa origen
3. Preview: programa detectado, campos mapeados, primeras 3 filas
4. Mapeo manual opcional (pickers para sobrescribir auto-detección)
5. Detección de duplicados (por referencia artículos, NIF clientes)
6. Importación con progreso
7. Resultado: importados, duplicados, errores + opción guardar perfil

### Archivos

```
domain/importador/CSVParser.kt          — Parser CSV con detección encoding/separador
domain/importador/MapeoUniversal.kt     — Diccionarios de sinónimos + auto-mapeo columnas
domain/importador/DetectorOrigen.kt     — Detecta programa origen (Salfon, Contaplus, Holded...)
domain/importador/ImportadorService.kt  — Lógica de importación con duplicados
ui/importador/ImportadorScreen.kt       — Pantalla principal
ui/importador/ImportadorViewModel.kt    — Estado + lógica
ui/importador/MapeoManualDialog.kt      — Dialog para mapeo manual
```

### Tipos CSV soportados

**Artículos:** Nombre*, Referencia, Precio, PrecioCoste, Unidad, Proveedor, Categoría, Descripción, TipoIVA
**Clientes:** Nombre*, NIF, Dirección, CP, Ciudad, Provincia, Teléfono, Email

### Entidad existente

`PerfilImportacion` ya existe en Room con: nombre, tipo, separador, mapeoJson, cabecerasOriginales, fechaCreacion, vecesUsado.

---

## 2. Scanner OCR

### Flujo

1. Abrir cámara con preview
2. ML Kit Text Recognition detecta texto en tiempo real
3. Usuario pulsa para capturar
4. Texto extraído se muestra para confirmación
5. "Usar este texto" → envía al chat de IA para procesar

### Archivos

```
ui/scanner/ScannerScreen.kt     — CameraX preview + ML Kit
ui/scanner/ScannerViewModel.kt  — Estado cámara + texto reconocido
```

### Dependencias

```kotlin
implementation("androidx.camera:camera-camera2:1.4.1")
implementation("androidx.camera:camera-lifecycle:1.4.1")
implementation("androidx.camera:camera-view:1.4.1")
implementation("com.google.mlkit:text-recognition:16.0.1")
```

### Permisos

`CAMERA` — pedir en runtime (mismo patrón que RECORD_AUDIO).

---

## 3. Fotos adjuntas a facturas

### Flujo

1. En FacturaDetalleScreen, botón "Fotos"
2. Abre gallery picker (max 5 fotos)
3. Comprime a JPEG 0.7 calidad, max 800px
4. Guarda en filesDir/fotos/{facturaId}/
5. Rutas guardadas en factura (campo nuevo o tabla separada)

### Archivos

```
ui/factura/components/FotosFacturaSheet.kt  — BottomSheet con grid de fotos + picker
```

### Almacenamiento

Fotos como archivos JPEG en `filesDir/fotos/{facturaId}/foto_N.jpg`.
Campo `fotosRutas` en Factura (List<String> serializado) — o tabla separada si preferimos normalizar.

Dado que Factura ya existe y no tiene campo de fotos, añadiremos un campo `fotosRutas: String = ""` (rutas separadas por `|||`, mismo patrón que etiquetas en Articulo).

---

## 4. Firma digital del cliente

### Flujo

1. En FacturaDetalleScreen, botón "Firma"
2. Abre canvas de dibujo (400x200)
3. Usuario firma con el dedo
4. Guarda como PNG en `filesDir/firmas/{facturaId}.png`
5. Ruta guardada en `factura.firmaClienteRuta`

### Archivos

```
ui/factura/components/FirmaDialog.kt  — Dialog con Canvas de dibujo
```

### Implementación Canvas

```kotlin
Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).pointerInput(Unit) {
    detectDragGestures { change, _ ->
        currentLine.add(change.position)
    }
}) {
    lines.forEach { line -> drawPath(line) }
}
```

Guardar: renderizar Path a Bitmap con `android.graphics.Canvas`, luego `compress(PNG)`.

---

## 5. Facturas recurrentes

### Flujo UI

1. Desde FacturaDetalleScreen, botón "Recurrente" → dialog frecuencia
2. Crea FacturaRecurrente con datos de la factura actual
3. RecurrentesScreen: lista con toggle activo/inactivo, frecuencia, próxima fecha
4. WorkManager: revisa diariamente si hay recurrentes pendientes → genera facturas automáticamente

### Archivos

```
ui/recurrentes/RecurrentesScreen.kt     — Lista de recurrentes
ui/recurrentes/RecurrentesViewModel.kt  — CRUD + generación
service/RecurrenteWorker.kt             — WorkManager worker diario
```

### Entidad existente

`FacturaRecurrente` ya existe con: nombre, clienteNombre, clienteNIF, articulosTexto, importeTotal, frecuencia, proximaFecha, activo, vecesGenerada.

### WorkManager (mejora sobre iOS)

```kotlin
class RecurrenteWorker : CoroutineWorker {
    // Cada día a las 8:00
    // Busca recurrentes donde activo=true Y proximaFecha <= hoy
    // Para cada una: crea factura borrador via ToolExecutor
    // Actualiza proximaFecha y vecesGenerada
}
```

---

## 6. Plantillas de factura

### Flujo

1. Desde FacturaDetalleScreen, botón "Plantilla" → guarda como plantilla
2. PlantillasScreen: lista de plantillas guardadas
3. Tap en plantilla → crea nueva factura borrador con esos datos

### Archivos

```
ui/plantillas/PlantillasScreen.kt     — Lista de plantillas
ui/plantillas/PlantillasViewModel.kt  — CRUD
```

### Entidad existente

`PlantillaFactura` ya existe con: nombre, articulosTexto, observaciones, vecesUsada, fechaCreacion.

---

## Navegación

Añadir rutas:
- `Rutas.IMPORTAR` → ImportadorScreen
- `Rutas.SCANNER` → ScannerScreen
- `Rutas.RECURRENTES` → RecurrentesScreen
- `Rutas.PLANTILLAS` → PlantillasScreen

Acceso:
- Importar: desde BandejaScreen (botón en TopAppBar o menú)
- Scanner: desde MainScreen (botón cámara en CommandInputBar)
- Recurrentes/Plantillas: desde FacturaDetalleScreen (AccionesRow) y desde BandejaScreen

---

## Campos nuevos en Factura

Añadir a la entidad Factura:
```kotlin
val fotosRutas: String = ""  // rutas separadas por "|||"
```

---

## Orden de implementación

1. Firma digital (más simple, solo Canvas + PNG)
2. Fotos adjuntas (gallery picker + compresión)
3. Plantillas (CRUD simple, entidad existe)
4. Recurrentes + WorkManager (entidad existe, añadir worker)
5. Scanner OCR (CameraX + ML Kit)
6. Importador CSV (más complejo)
