# EhFacturas Android — Spec Fase 2 Gaps

## Resumen

Completar los 5 gaps pendientes de Fase 2 para paridad con iOS.

---

## 1. Tab Informes — `ui/informes/InformesScreen.kt`

### Selector de periodo

Chips horizontales: **Este mes | Trimestre | Este año | Todo**

Cálculo de fechas:
- Este mes: primer día del mes actual → hoy
- Trimestre: primer día del trimestre actual → hoy
- Este año: 1 de enero → hoy
- Todo: Date(0) → hoy

### Resumen (cards grid 2 columnas)

| Stat | Cálculo |
|------|---------|
| Facturado | SUM(totalFactura) donde estado IN (EMITIDA, PAGADA) |
| Cobrado | SUM(totalFactura) donde estado = PAGADA |
| Pendiente | Facturado - Cobrado |
| IVA repercutido | SUM(totalIVA) donde estado IN (EMITIDA, PAGADA) |
| IRPF retenido | SUM(totalIRPF) donde estado IN (EMITIDA, PAGADA), solo si > 0 |
| Gastos | SUM(importe) de gastos en periodo |
| Beneficio neto | Facturado - IRPF |
| Beneficio real | Beneficio neto - Gastos |

### Gráfico mensual

Barras dobles por mes usando **Vico** (librería Compose Charts nativa):
- Barra azul: facturado del mes
- Barra verde: cobrado del mes
- Eje X: nombres de meses abreviados (Ene, Feb...)
- Eje Y: euros

Dependencia: `implementation("com.patrykandpatrick.vico:compose-m3:2.1.0")`

### Top 5 clientes

Lista ordenada por SUM(totalFactura) DESC, agrupada por clienteNombre.
Muestra: nombre + total facturado en el periodo.

### Gastos por categoría

Agrupa gastos del periodo por `categoria`. Muestra: icono categoría + nombre + count + total.

### Exportar CSV

Botón "Exportar facturas (CSV)" que genera:
```
N° Factura;Fecha;Cliente;NIF Cliente;Base Imponible;IVA;IRPF;Total;Estado
```
Comparte via `Intent.ACTION_SEND` con tipo `text/csv`.

### Archivos

```
ui/informes/InformesScreen.kt
ui/informes/InformesViewModel.kt
```

### ViewModel

```kotlin
data class InformesUiState(
    val periodo: Periodo,
    val facturado: Double,
    val cobrado: Double,
    val pendiente: Double,
    val ivaRepercutido: Double,
    val irpfRetenido: Double,
    val gastosPeriodo: Double,
    val beneficioNeto: Double,
    val beneficioReal: Double,
    val facturasEmitidas: Int,
    val datosMensuales: List<DatoMensual>,  // para el gráfico
    val topClientes: List<Pair<String, Double>>,
    val gastosPorCategoria: List<Pair<String, Double>>
)

enum class Periodo { MES, TRIMESTRE, ANO, TODO }

data class DatoMensual(
    val mes: String,
    val facturado: Double,
    val cobrado: Double
)
```

Queries necesarias (añadir a FacturaDao/GastoDao si no existen):
- `facturacionPorEstadoYPeriodo(estados, desde, hasta)` → Double
- `facturacionMensual(desde, hasta)` → List de mes + suma
- `topClientes(desde, hasta, limit)` → List de nombre + suma
- `gastosPorCategoria(desde, hasta)` → List de categoría + suma

---

## 2. AjustesScreen — `ui/settings/AjustesScreen.kt`

Pantalla Compose con LazyColumn de secciones.

### Secciones

**1. Logo**
- PhotoPicker para seleccionar imagen
- Redimensionar a max 400px
- Guardar ruta en `negocio.logoRuta`

**2. Identidad fiscal**
- Nombre / Razón social (OutlinedTextField)
- NIF / CIF (OutlinedTextField con validación NifValidator)

**3. Contacto**
- Teléfono (keyboardType = Phone)
- Email (keyboardType = Email, validación)

**4. Dirección**
- Dirección
- C.P. + Ciudad (Row)
- Provincia

**5. Impuestos**
- IVA general: Text display "21%" (no editable)
- Toggle: "Aplicar retención IRPF"
- Si activo: selector 7% (primeros 3 años) / 15% (general)
- Texto explicativo debajo

**6. Numeración de facturas**
- Prefijo (editable)
- Siguiente número (display)
- Vista previa: "${prefijo}${numero.padStart(4, '0')}"

**7. Condiciones de pago**
- TextField multilínea para notas de pago

**8. Apariencia**
- SegmentedButton: Auto / Claro / Oscuro
- Aplica `negocio.temaApp` y actualiza AppPreferences

**9. Voz de la IA**
- Toggle: "Voz activada"
- Si activo: selector de tipo de voz
- Botón "Probar voz"

**10. Inteligencia Artificial**
- Selector: Claude / OpenAI
- Campo API key (solo en modo debug)

**11. Copia de seguridad**
- Botón "Exportar datos (JSON)"
- Exporta clientes + artículos + gastos via Intent

**12. Acerca de**
- Versión de la app

### Archivos

```
ui/settings/AjustesScreen.kt
ui/settings/AjustesViewModel.kt
```

### ViewModel

```kotlin
@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val negocioRepository: NegocioRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val negocio: StateFlow<Negocio?>
    fun guardarNegocio(negocio: Negocio)
    fun setTema(tema: String)
    fun exportarDatos(): String  // JSON
}
```

### Navegación

Añadir ruta `Rutas.AJUSTES` a AppNavigation. Botón de acceso desde MainScreen TopAppBar (icono Settings) o desde BandejaScreen.

---

## 3. DatePicker en FacturaEditScreen

Añadir `DatePickerDialog` de Material 3 al pulsar los campos de fecha.

### Implementación

```kotlin
// En FacturaEditScreen, al pulsar campo fecha:
var mostrarDatePicker by remember { mutableStateOf(false) }

if (mostrarDatePicker) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.factura.fecha.time
    )
    DatePickerDialog(
        onDismissRequest = { mostrarDatePicker = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    viewModel.setFecha(Date(it))
                }
                mostrarDatePicker = false
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
```

Mismo patrón para `fechaVencimiento`. Hacer los OutlinedTextField clickables con `Modifier.clickable`.

### Archivos modificados

```
ui/factura/FacturaEditScreen.kt
```

---

## 4. Compartir PDF

### Implementación

En `FacturaDetalleScreen`, el botón PDF:
1. Llama `viewModel.generarPdf()` (ya existe)
2. Cuando `factura.pdfRuta` no es null, abrir share sheet

```kotlin
fun compartirPdf(context: Context, pdfPath: String) {
    val file = File(pdfPath)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir factura"))
}
```

### Archivos modificados

```
ui/factura/FacturaDetalleScreen.kt
ui/factura/FacturaDetalleViewModel.kt (añadir evento compartir)
```

---

## 5. QR en PDF

### Implementación

Dependencia ya incluida: `com.google.zxing:core:3.5.3`

En `FacturaPdfGenerator.kt`, añadir método:

```kotlin
private fun generarQRBitmap(contenido: String, size: Int): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )
    val matrix = MultiFormatWriter().encode(
        contenido, BarcodeFormat.QR_CODE, size, size, hints
    )
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
```

Dibujado al final del PDF, antes del footer:
```kotlin
if (registro != null && registro.hashRegistro.isNotEmpty()) {
    val qrBitmap = generarQRBitmap(registro.hashRegistro, 100)
    canvas.drawBitmap(qrBitmap, MARGIN, y, null)
    y += 110f
    canvas.drawText("Hash: ${registro.hashRegistro}", MARGIN + 110f, y - 60f, paintGris)
}
```

### Archivos modificados

```
domain/pdf/FacturaPdfGenerator.kt
```

---

## Queries nuevas necesarias

### FacturaDao — añadir:

```kotlin
@Query("""
    SELECT SUM(totalFactura) FROM facturas
    WHERE estado IN (:estados) AND fecha BETWEEN :desde AND :hasta
""")
fun facturacionPorEstados(estados: List<String>, desde: Date, hasta: Date): Flow<Double?>

@Query("""
    SELECT clienteNombre, SUM(totalFactura) as total FROM facturas
    WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta
    GROUP BY clienteNombre ORDER BY total DESC LIMIT :limit
""")
suspend fun topClientes(desde: Date, hasta: Date, limit: Int): List<ClienteTotal>

// Data class auxiliar
data class ClienteTotal(val clienteNombre: String, val total: Double)
```

### GastoDao — añadir:

```kotlin
@Query("""
    SELECT categoria, SUM(importe) as total FROM gastos
    WHERE fecha BETWEEN :desde AND :hasta
    GROUP BY categoria ORDER BY total DESC
""")
suspend fun gastosPorCategoria(desde: Date, hasta: Date): List<CategoriaTotal>

@Query("SELECT SUM(importe) FROM gastos WHERE fecha BETWEEN :desde AND :hasta")
fun totalGastos(desde: Date, hasta: Date): Flow<Double?>

data class CategoriaTotal(val categoria: String, val total: Double)
```

---

## Dependencias nuevas

```toml
# Vico Charts para gráfico mensual
vico-compose = "com.patrykandpatrick.vico:compose-m3:2.1.0"
```

---

## Orden de implementación

1. Queries DAO (sin dependencias)
2. QR en PDF (modificar FacturaPdfGenerator)
3. DatePicker (modificar FacturaEditScreen)
4. Compartir PDF (modificar FacturaDetalleScreen)
5. AjustesScreen + ViewModel + navegación
6. InformesScreen + ViewModel + gráfico Vico
