# Phase 2 Gaps Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete 5 remaining Phase 2 gaps: DAO queries, QR in PDF, DatePicker, PDF sharing, AjustesScreen, InformesScreen with charts.

**Architecture:** Add new DAO queries for aggregation, modify existing screens for DatePicker/sharing, create two new screens (Ajustes, Informes) with Hilt ViewModels following existing MVVM patterns.

**Tech Stack:** Kotlin 2.0, Jetpack Compose, Material 3, Room, Hilt, Vico Charts, ZXing

---

## File Map

### New files
```
app/src/main/java/es/ehfacturas/ui/informes/InformesScreen.kt
app/src/main/java/es/ehfacturas/ui/informes/InformesViewModel.kt
app/src/main/java/es/ehfacturas/ui/settings/AjustesScreen.kt
app/src/main/java/es/ehfacturas/ui/settings/AjustesViewModel.kt
```

### Modified files
```
app/src/main/java/es/ehfacturas/data/db/dao/FacturaDao.kt
app/src/main/java/es/ehfacturas/data/db/dao/GastoDao.kt
app/src/main/java/es/ehfacturas/data/repository/FacturaRepository.kt
app/src/main/java/es/ehfacturas/data/repository/GastoRepository.kt
app/src/main/java/es/ehfacturas/domain/pdf/FacturaPdfGenerator.kt
app/src/main/java/es/ehfacturas/ui/factura/FacturaEditScreen.kt
app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleScreen.kt
app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleViewModel.kt
app/src/main/java/es/ehfacturas/ui/bandeja/BandejaScreen.kt
app/src/main/java/es/ehfacturas/ui/navigation/AppNavigation.kt
app/build.gradle.kts
```

---

### Task 1: Add DAO queries for reports

**Files:**
- Modify: `app/src/main/java/es/ehfacturas/data/db/dao/FacturaDao.kt`
- Modify: `app/src/main/java/es/ehfacturas/data/db/dao/GastoDao.kt`
- Modify: `app/src/main/java/es/ehfacturas/data/repository/FacturaRepository.kt`
- Modify: `app/src/main/java/es/ehfacturas/data/repository/GastoRepository.kt`

- [ ] **Step 1: Add queries to FacturaDao**

Add these methods before the `insertar` method in `FacturaDao.kt`:

```kotlin
data class ClienteTotal(
    val clienteNombre: String,
    val total: Double
)

data class FacturaMensual(
    val mes: Int,
    val ano: Int,
    val facturado: Double,
    val cobrado: Double
)
```

Add above the `@Dao` interface declaration. Then add inside the interface:

```kotlin
@Query("SELECT COALESCE(SUM(totalFactura), 0) FROM facturas WHERE estado = 'PAGADA' AND fecha BETWEEN :desde AND :hasta")
fun cobradoPeriodo(desde: Date, hasta: Date): Flow<Double>

@Query("SELECT COALESCE(SUM(totalIVA), 0) FROM facturas WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta")
fun ivaPeriodo(desde: Date, hasta: Date): Flow<Double>

@Query("SELECT COALESCE(SUM(totalIRPF), 0) FROM facturas WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta")
fun irpfPeriodo(desde: Date, hasta: Date): Flow<Double>

@Query("SELECT COUNT(*) FROM facturas WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta")
fun contarEmitidasPeriodo(desde: Date, hasta: Date): Flow<Int>

@Query("""
    SELECT clienteNombre, SUM(totalFactura) as total FROM facturas
    WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta
    GROUP BY clienteNombre ORDER BY total DESC LIMIT :limit
""")
suspend fun topClientes(desde: Date, hasta: Date, limit: Int): List<ClienteTotal>

@Query("""
    SELECT * FROM facturas
    WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta
    ORDER BY fecha ASC
""")
suspend fun facturasParaExportar(desde: Date, hasta: Date): List<Factura>
```

- [ ] **Step 2: Add queries to GastoDao**

Add data class before the interface:

```kotlin
data class CategoriaTotal(
    val categoria: String,
    val total: Double
)
```

Add inside the interface:

```kotlin
@Query("""
    SELECT categoria, COALESCE(SUM(importe), 0) as total FROM gastos
    WHERE fecha BETWEEN :desde AND :hasta
    GROUP BY categoria ORDER BY total DESC
""")
suspend fun gastosPorCategoria(desde: Date, hasta: Date): List<CategoriaTotal>
```

- [ ] **Step 3: Add repository methods**

In `FacturaRepository.kt`, add:

```kotlin
fun cobradoPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.cobradoPeriodo(desde, hasta)
fun ivaPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.ivaPeriodo(desde, hasta)
fun irpfPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.irpfPeriodo(desde, hasta)
fun contarEmitidasPeriodo(desde: Date, hasta: Date): Flow<Int> = facturaDao.contarEmitidasPeriodo(desde, hasta)
suspend fun topClientes(desde: Date, hasta: Date, limit: Int): List<ClienteTotal> = facturaDao.topClientes(desde, hasta, limit)
suspend fun facturasParaExportar(desde: Date, hasta: Date): List<Factura> = facturaDao.facturasParaExportar(desde, hasta)
```

Add import: `import es.ehfacturas.data.db.dao.ClienteTotal`

In `GastoRepository.kt`, add:

```kotlin
suspend fun gastosPorCategoria(desde: Date, hasta: Date): List<CategoriaTotal> = gastoDao.gastosPorCategoria(desde, hasta)
```

Add import: `import es.ehfacturas.data.db.dao.CategoriaTotal`

- [ ] **Step 4: Build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/es/ehfacturas/data/db/dao/FacturaDao.kt \
       app/src/main/java/es/ehfacturas/data/db/dao/GastoDao.kt \
       app/src/main/java/es/ehfacturas/data/repository/FacturaRepository.kt \
       app/src/main/java/es/ehfacturas/data/repository/GastoRepository.kt
git commit -m "feat: add DAO queries for reports (top clients, IVA, IRPF, expenses by category)"
```

---

### Task 2: QR code in PDF

**Files:**
- Modify: `app/src/main/java/es/ehfacturas/domain/pdf/FacturaPdfGenerator.kt`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add ZXing dependency**

In `app/build.gradle.kts`, add in the `dependencies` block:

```kotlin
implementation("com.google.zxing:core:3.5.3")
```

- [ ] **Step 2: Add QR generation method to FacturaPdfGenerator**

Add these imports at the top of `FacturaPdfGenerator.kt`:

```kotlin
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
```

Add this private method to the class:

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

- [ ] **Step 3: Replace hash text with QR + hash in the `generar()` method**

Find the section in `generar()` that draws the hash text and replace it with:

```kotlin
// QR + Hash
if (registro != null && registro.hashRegistro.isNotEmpty()) {
    y += 10f
    val qrBitmap = generarQRBitmap(registro.hashRegistro, 100)
    canvas.drawBitmap(qrBitmap, MARGIN, y, null)
    canvas.drawText("Hash: ${registro.hashRegistro}", MARGIN + 110f, y + 50f, paintGris)
    y += 110f
}
```

- [ ] **Step 4: Build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/es/ehfacturas/domain/pdf/FacturaPdfGenerator.kt app/build.gradle.kts
git commit -m "feat: add QR code with hash to PDF invoices"
```

---

### Task 3: DatePicker in FacturaEditScreen

**Files:**
- Modify: `app/src/main/java/es/ehfacturas/ui/factura/FacturaEditScreen.kt`

- [ ] **Step 1: Add DatePicker dialogs**

Read the current `FacturaEditScreen.kt` fully, then make these changes:

Add state variables after `var mostrarSelectorArticulo`:

```kotlin
var mostrarDatePickerFecha by remember { mutableStateOf(false) }
var mostrarDatePickerVencimiento by remember { mutableStateOf(false) }
```

Replace the two date `OutlinedTextField` fields (the Row with "Fecha" and "Vencimiento") with clickable versions:

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    OutlinedTextField(
        value = formatoFecha.format(uiState.factura.fecha),
        onValueChange = {},
        label = { Text("Fecha") },
        modifier = Modifier
            .weight(1f)
            .clickable { mostrarDatePickerFecha = true },
        readOnly = true,
        enabled = false
    )
    OutlinedTextField(
        value = uiState.factura.fechaVencimiento?.let { formatoFecha.format(it) } ?: "",
        onValueChange = {},
        label = { Text("Vencimiento") },
        modifier = Modifier
            .weight(1f)
            .clickable { mostrarDatePickerVencimiento = true },
        readOnly = true,
        enabled = false
    )
}

// DatePicker fecha
if (mostrarDatePickerFecha) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.factura.fecha.time
    )
    DatePickerDialog(
        onDismissRequest = { mostrarDatePickerFecha = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    viewModel.setFecha(Date(it))
                }
                mostrarDatePickerFecha = false
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = { mostrarDatePickerFecha = false }) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// DatePicker vencimiento
if (mostrarDatePickerVencimiento) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.factura.fechaVencimiento?.time
            ?: (uiState.factura.fecha.time + 30L * 24 * 60 * 60 * 1000)
    )
    DatePickerDialog(
        onDismissRequest = { mostrarDatePickerVencimiento = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    viewModel.setFechaVencimiento(Date(it))
                }
                mostrarDatePickerVencimiento = false
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = { mostrarDatePickerVencimiento = false }) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
```

Add import: `import androidx.compose.foundation.clickable`

- [ ] **Step 2: Build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/es/ehfacturas/ui/factura/FacturaEditScreen.kt
git commit -m "feat: add Material 3 DatePicker to invoice date fields"
```

---

### Task 4: PDF sharing

**Files:**
- Modify: `app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleScreen.kt`
- Modify: `app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleViewModel.kt`

- [ ] **Step 1: Add share event to FacturaDetalleViewModel**

Add to `FacturaDetalleUiState`:

```kotlin
val pdfParaCompartir: String? = null  // ruta del PDF a compartir
```

Add method to ViewModel:

```kotlin
fun generarYCompartirPdf() {
    viewModelScope.launch {
        val state = _uiState.value
        val factura = state.factura ?: return@launch
        val registro = state.registros.firstOrNull()

        val archivo = pdfGenerator.generar(factura, state.lineas, registro)
        val facturaConPdf = factura.copy(pdfRuta = archivo.absolutePath)
        facturaRepository.actualizar(facturaConPdf)
        _uiState.update { it.copy(factura = facturaConPdf, pdfParaCompartir = archivo.absolutePath) }
    }
}

fun limpiarCompartir() {
    _uiState.update { it.copy(pdfParaCompartir = null) }
}
```

- [ ] **Step 2: Add share logic to FacturaDetalleScreen**

Add imports:

```kotlin
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
```

Add after the `LaunchedEffect(uiState.facturaIdNueva)` block:

```kotlin
val context = LocalContext.current

// Compartir PDF
LaunchedEffect(uiState.pdfParaCompartir) {
    uiState.pdfParaCompartir?.let { pdfPath ->
        val file = File(pdfPath)
        if (file.exists()) {
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
        viewModel.limpiarCompartir()
    }
}
```

Update the `onPdf` callback in `AccionesRow`:

```kotlin
onPdf = { viewModel.generarYCompartirPdf() },
```

- [ ] **Step 3: Build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleScreen.kt \
       app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleViewModel.kt
git commit -m "feat: add PDF generation and sharing via FileProvider"
```

---

### Task 5: AjustesScreen + ViewModel

**Files:**
- Create: `app/src/main/java/es/ehfacturas/ui/settings/AjustesViewModel.kt`
- Create: `app/src/main/java/es/ehfacturas/ui/settings/AjustesScreen.kt`

- [ ] **Step 1: Create AjustesViewModel**

```kotlin
package es.ehfacturas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.Negocio
import es.ehfacturas.data.preferences.AppPreferences
import es.ehfacturas.data.repository.ArticuloRepository
import es.ehfacturas.data.repository.ClienteRepository
import es.ehfacturas.data.repository.GastoRepository
import es.ehfacturas.data.repository.NegocioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val negocioRepository: NegocioRepository,
    private val clienteRepository: ClienteRepository,
    private val articuloRepository: ArticuloRepository,
    private val gastoRepository: GastoRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val negocio: StateFlow<Negocio?> = negocioRepository.obtenerNegocio()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val temaApp: StateFlow<String> = appPreferences.temaApp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    val cloudProvider: StateFlow<String> = appPreferences.cloudProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "claude")

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun guardarNegocio(negocio: Negocio) {
        viewModelScope.launch {
            if (negocio.id == 0L) {
                negocioRepository.guardar(negocio)
            } else {
                negocioRepository.actualizar(negocio)
            }
            _mensaje.value = "Guardado"
        }
    }

    fun setTema(tema: String) {
        viewModelScope.launch { appPreferences.setTemaApp(tema) }
    }

    fun setCloudProvider(provider: String) {
        viewModelScope.launch { appPreferences.setCloudProvider(provider) }
    }

    fun exportarDatos(): String {
        // Se ejecutará en coroutine desde la UI
        return "{}" // Placeholder — se implementa en Step 2
    }

    fun exportarDatosAsync(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val clientes = clienteRepository.obtenerTodosSync()
            val articulos = articuloRepository.obtenerTodosSync()
            val gastos = gastoRepository.obtenerTodosSync()

            val json = JSONObject().apply {
                put("clientes", JSONArray().apply {
                    clientes.forEach { c ->
                        put(JSONObject().apply {
                            put("nombre", c.nombre)
                            put("nif", c.nif)
                            put("email", c.email)
                            put("telefono", c.telefono)
                            put("direccion", c.direccion)
                            put("ciudad", c.ciudad)
                            put("provincia", c.provincia)
                        })
                    }
                })
                put("articulos", JSONArray().apply {
                    articulos.forEach { a ->
                        put(JSONObject().apply {
                            put("nombre", a.nombre)
                            put("referencia", a.referencia)
                            put("precioUnitario", a.precioUnitario)
                            put("unidad", a.unidad.abreviatura)
                        })
                    }
                })
                put("gastos", JSONArray().apply {
                    gastos.forEach { g ->
                        put(JSONObject().apply {
                            put("concepto", g.concepto)
                            put("importe", g.importe)
                            put("categoria", g.categoria)
                        })
                    }
                })
            }
            onResult(json.toString(2))
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}
```

**NOTE:** This requires `obtenerTodosSync()` suspend methods on ClienteRepository, ArticuloRepository, GastoRepository. If they don't exist, add them:

In `ClienteRepository.kt`:
```kotlin
suspend fun obtenerTodosSync(): List<Cliente> = clienteDao.obtenerTodosSync()
```
In `ClienteDao.kt`:
```kotlin
@Query("SELECT * FROM clientes WHERE activo = 1")
suspend fun obtenerTodosSync(): List<Cliente>
```

Same pattern for `ArticuloRepository`/`ArticuloDao` and `GastoRepository`/`GastoDao`.

- [ ] **Step 2: Create AjustesScreen**

```kotlin
package es.ehfacturas.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.Negocio
import es.ehfacturas.domain.validation.NifValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onBack: () -> Unit,
    viewModel: AjustesViewModel = hiltViewModel()
) {
    val negocioActual by viewModel.negocio.collectAsStateWithLifecycle()
    val temaApp by viewModel.temaApp.collectAsStateWithLifecycle()
    val cloudProvider by viewModel.cloudProvider.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Estado editable del negocio
    var nombre by remember(negocioActual) { mutableStateOf(negocioActual?.nombre ?: "") }
    var nif by remember(negocioActual) { mutableStateOf(negocioActual?.nif ?: "") }
    var telefono by remember(negocioActual) { mutableStateOf(negocioActual?.telefono ?: "") }
    var email by remember(negocioActual) { mutableStateOf(negocioActual?.email ?: "") }
    var direccion by remember(negocioActual) { mutableStateOf(negocioActual?.direccion ?: "") }
    var codigoPostal by remember(negocioActual) { mutableStateOf(negocioActual?.codigoPostal ?: "") }
    var ciudad by remember(negocioActual) { mutableStateOf(negocioActual?.ciudad ?: "") }
    var provincia by remember(negocioActual) { mutableStateOf(negocioActual?.provincia ?: "") }
    var aplicarIRPF by remember(negocioActual) { mutableStateOf(negocioActual?.aplicarIRPF ?: false) }
    var irpfPorcentaje by remember(negocioActual) { mutableStateOf(negocioActual?.irpfPorcentaje ?: 15.0) }
    var prefijoFactura by remember(negocioActual) { mutableStateOf(negocioActual?.prefijoFactura ?: "FAC-") }
    var notas by remember(negocioActual) { mutableStateOf(negocioActual?.notas ?: "") }
    var errorNif by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (!NifValidator.esValido(nif)) {
                            errorNif = "NIF/CIF no válido"
                            return@TextButton
                        }
                        errorNif = null
                        val negocio = (negocioActual ?: Negocio()).copy(
                            nombre = nombre, nif = nif, telefono = telefono, email = email,
                            direccion = direccion, codigoPostal = codigoPostal, ciudad = ciudad,
                            provincia = provincia, aplicarIRPF = aplicarIRPF,
                            irpfPorcentaje = irpfPorcentaje, prefijoFactura = prefijoFactura,
                            notas = notas
                        )
                        viewModel.guardarNegocio(negocio)
                    }) { Text("Guardar") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Identidad fiscal
            item {
                Text("Identidad fiscal", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            }
            item {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre / Razón social") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                OutlinedTextField(value = nif, onValueChange = { nif = it; errorNif = null },
                    label = { Text("NIF / CIF") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    isError = errorNif != null,
                    supportingText = errorNif?.let { { Text(it) } })
            }

            // Contacto
            item {
                Text("Contacto", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedTextField(value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            }
            item {
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            }

            // Dirección
            item {
                Text("Dirección", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedTextField(value = direccion, onValueChange = { direccion = it },
                    label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = codigoPostal, onValueChange = { codigoPostal = it },
                        label = { Text("C.P.") }, modifier = Modifier.weight(1f), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = ciudad, onValueChange = { ciudad = it },
                        label = { Text("Ciudad") }, modifier = Modifier.weight(2f), singleLine = true)
                }
            }
            item {
                OutlinedTextField(value = provincia, onValueChange = { provincia = it },
                    label = { Text("Provincia") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }

            // Impuestos
            item {
                Text("Impuestos", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Text("IVA general: 21%", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Aplicar retención IRPF", modifier = Modifier.weight(1f))
                    Switch(checked = aplicarIRPF, onCheckedChange = { aplicarIRPF = it })
                }
            }
            if (aplicarIRPF) {
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(selected = irpfPorcentaje == 7.0,
                            onClick = { irpfPorcentaje = 7.0 }, shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text("7% (nuevos)") }
                        SegmentedButton(selected = irpfPorcentaje == 15.0,
                            onClick = { irpfPorcentaje = 15.0 }, shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text("15% (general)") }
                    }
                }
                item {
                    Text("7% los primeros 3 años de actividad, 15% a partir del 4o.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Numeración
            item {
                Text("Numeración de facturas", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedTextField(value = prefijoFactura, onValueChange = { prefijoFactura = it },
                    label = { Text("Prefijo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                val siguiente = negocioActual?.siguienteNumero ?: 1
                Text("Siguiente: $prefijoFactura${siguiente.toString().padStart(4, '0')}",
                    style = MaterialTheme.typography.bodyMedium)
            }

            // Condiciones de pago
            item {
                Text("Condiciones de pago", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedTextField(value = notas, onValueChange = { notas = it },
                    label = { Text("Notas de pago") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            }

            // Apariencia
            item {
                Text("Apariencia", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("auto" to "Auto", "claro" to "Claro", "oscuro" to "Oscuro").forEachIndexed { i, (value, label) ->
                        SegmentedButton(selected = temaApp == value,
                            onClick = { viewModel.setTema(value) },
                            shape = SegmentedButtonDefaults.itemShape(i, 3)
                        ) { Text(label) }
                    }
                }
            }

            // IA
            item {
                Text("Inteligencia Artificial", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("claude" to "Claude", "openai" to "OpenAI").forEachIndexed { i, (value, label) ->
                        SegmentedButton(selected = cloudProvider == value,
                            onClick = { viewModel.setCloudProvider(value) },
                            shape = SegmentedButtonDefaults.itemShape(i, 2)
                        ) { Text(label) }
                    }
                }
            }

            // Backup
            item {
                Text("Copia de seguridad", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedButton(onClick = {
                    viewModel.exportarDatosAsync { json ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar datos"))
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Exportar datos (JSON)")
                }
            }

            // Acerca de
            item {
                Text("Acerca de", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Text("EhFacturas! Android v1.0.0", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
```

- [ ] **Step 3: Add sync DAO methods if missing**

Check if `obtenerTodosSync()` exists on ClienteDao, ArticuloDao, GastoDao. If not, add:

`ClienteDao.kt`:
```kotlin
@Query("SELECT * FROM clientes WHERE activo = 1")
suspend fun obtenerTodosSync(): List<Cliente>
```

`ArticuloDao.kt`:
```kotlin
@Query("SELECT * FROM articulos WHERE activo = 1")
suspend fun obtenerTodosSync(): List<Articulo>
```

`GastoDao.kt`:
```kotlin
@Query("SELECT * FROM gastos")
suspend fun obtenerTodosSync(): List<Gasto>
```

And corresponding repository methods.

- [ ] **Step 4: Build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/es/ehfacturas/ui/settings/ \
       app/src/main/java/es/ehfacturas/data/db/dao/ClienteDao.kt \
       app/src/main/java/es/ehfacturas/data/db/dao/ArticuloDao.kt \
       app/src/main/java/es/ehfacturas/data/db/dao/GastoDao.kt \
       app/src/main/java/es/ehfacturas/data/repository/ClienteRepository.kt \
       app/src/main/java/es/ehfacturas/data/repository/ArticuloRepository.kt \
       app/src/main/java/es/ehfacturas/data/repository/GastoRepository.kt
git commit -m "feat: add AjustesScreen with business settings, taxes, appearance, AI, backup"
```

---

### Task 6: Navigation for Ajustes

**Files:**
- Modify: `app/src/main/java/es/ehfacturas/ui/navigation/AppNavigation.kt`
- Modify: `app/src/main/java/es/ehfacturas/ui/bandeja/BandejaScreen.kt`

- [ ] **Step 1: Add Ajustes route to AppNavigation**

Add import: `import es.ehfacturas.ui.settings.AjustesScreen`

Add composable before the closing `}` of NavHost:

```kotlin
composable(Rutas.AJUSTES) {
    AjustesScreen(
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 2: Wire settings button in BandejaScreen**

Add `onNavigateToAjustes: () -> Unit = {}` parameter to BandejaScreen.

Update the Settings icon button:

```kotlin
IconButton(onClick = onNavigateToAjustes) {
    Icon(Icons.Default.Settings, contentDescription = "Ajustes")
}
```

Update BandejaScreen call in AppNavigation to pass:

```kotlin
BandejaScreen(
    onNavigateBack = { navController.popBackStack() },
    onNavigateToFactura = { ... },
    onNavigateToNuevaFactura = { ... },
    onNavigateToAjustes = { navController.navigate(Rutas.AJUSTES) }
)
```

- [ ] **Step 3: Build and commit**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`

```bash
git add app/src/main/java/es/ehfacturas/ui/navigation/AppNavigation.kt \
       app/src/main/java/es/ehfacturas/ui/bandeja/BandejaScreen.kt
git commit -m "feat: wire Ajustes navigation from BandejaScreen"
```

---

### Task 7: InformesViewModel

**Files:**
- Create: `app/src/main/java/es/ehfacturas/ui/informes/InformesViewModel.kt`

- [ ] **Step 1: Create InformesViewModel**

```kotlin
package es.ehfacturas.ui.informes

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.repository.FacturaRepository
import es.ehfacturas.data.repository.GastoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class Periodo(val descripcion: String) {
    MES("Este mes"),
    TRIMESTRE("Trimestre"),
    ANO("Este año"),
    TODO("Todo")
}

data class DatoMensual(
    val mes: String,
    val facturado: Double,
    val cobrado: Double
)

data class InformesUiState(
    val periodo: Periodo = Periodo.MES,
    val facturado: Double = 0.0,
    val cobrado: Double = 0.0,
    val pendiente: Double = 0.0,
    val ivaRepercutido: Double = 0.0,
    val irpfRetenido: Double = 0.0,
    val gastosPeriodo: Double = 0.0,
    val beneficioNeto: Double = 0.0,
    val beneficioReal: Double = 0.0,
    val facturasEmitidas: Int = 0,
    val topClientes: List<Pair<String, Double>> = emptyList(),
    val gastosPorCategoria: List<Pair<String, Double>> = emptyList(),
    val csvParaCompartir: String? = null
)

@HiltViewModel
class InformesViewModel @Inject constructor(
    private val facturaRepository: FacturaRepository,
    private val gastoRepository: GastoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InformesUiState())
    val uiState: StateFlow<InformesUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    init {
        setPeriodo(Periodo.MES)
    }

    fun setPeriodo(periodo: Periodo) {
        _uiState.update { it.copy(periodo = periodo) }
        cargarDatos(periodo)
    }

    private fun fechaInicio(periodo: Periodo): Date {
        val cal = Calendar.getInstance()
        return when (periodo) {
            Periodo.MES -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                cal.time
            }
            Periodo.TRIMESTRE -> {
                val month = cal.get(Calendar.MONTH)
                val quarterStart = (month / 3) * 3
                cal.set(Calendar.MONTH, quarterStart)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                cal.time
            }
            Periodo.ANO -> {
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                cal.time
            }
            Periodo.TODO -> Date(0)
        }
    }

    private fun cargarDatos(periodo: Periodo) {
        val desde = fechaInicio(periodo)
        val hasta = Date()

        // Facturado
        viewModelScope.launch {
            facturaRepository.facturacionPeriodo(desde, hasta).collect { facturado ->
                _uiState.update { it.copy(facturado = facturado) }
            }
        }
        // Cobrado
        viewModelScope.launch {
            facturaRepository.cobradoPeriodo(desde, hasta).collect { cobrado ->
                _uiState.update { state ->
                    state.copy(
                        cobrado = cobrado,
                        pendiente = state.facturado - cobrado
                    )
                }
            }
        }
        // IVA
        viewModelScope.launch {
            facturaRepository.ivaPeriodo(desde, hasta).collect { iva ->
                _uiState.update { it.copy(ivaRepercutido = iva) }
            }
        }
        // IRPF
        viewModelScope.launch {
            facturaRepository.irpfPeriodo(desde, hasta).collect { irpf ->
                _uiState.update { state ->
                    state.copy(
                        irpfRetenido = irpf,
                        beneficioNeto = state.facturado - irpf,
                        beneficioReal = state.facturado - irpf - state.gastosPeriodo
                    )
                }
            }
        }
        // Count
        viewModelScope.launch {
            facturaRepository.contarEmitidasPeriodo(desde, hasta).collect { count ->
                _uiState.update { it.copy(facturasEmitidas = count) }
            }
        }
        // Gastos
        viewModelScope.launch {
            gastoRepository.totalPeriodo(desde, hasta).collect { gastos ->
                _uiState.update { state ->
                    state.copy(
                        gastosPeriodo = gastos,
                        beneficioReal = state.beneficioNeto - gastos
                    )
                }
            }
        }
        // Top clientes
        viewModelScope.launch {
            val top = facturaRepository.topClientes(desde, hasta, 5)
            _uiState.update { it.copy(topClientes = top.map { c -> c.clienteNombre to c.total }) }
        }
        // Gastos por categoría
        viewModelScope.launch {
            val categorias = gastoRepository.gastosPorCategoria(desde, hasta)
            _uiState.update { it.copy(gastosPorCategoria = categorias.map { c -> c.categoria to c.total }) }
        }
    }

    fun exportarCSV() {
        val desde = fechaInicio(_uiState.value.periodo)
        val hasta = Date()
        viewModelScope.launch {
            val facturas = facturaRepository.facturasParaExportar(desde, hasta)
            val csv = buildString {
                appendLine("N° Factura;Fecha;Cliente;NIF Cliente;Base Imponible;IVA;IRPF;Total;Estado")
                facturas.forEach { f ->
                    appendLine("${f.numeroFactura};${formatoFecha.format(f.fecha)};${f.clienteNombre};${f.clienteNIF};${f.baseImponible};${f.totalIVA};${f.totalIRPF};${f.totalFactura};${f.estado.descripcion}")
                }
            }
            _uiState.update { it.copy(csvParaCompartir = csv) }
        }
    }

    fun limpiarCSV() {
        _uiState.update { it.copy(csvParaCompartir = null) }
    }
}
```

- [ ] **Step 2: Add missing repository methods**

Add to `GastoRepository.kt` if not present:
```kotlin
fun totalPeriodo(desde: Date, hasta: Date): Flow<Double> = gastoDao.totalPeriodo(desde, hasta)
```

Add to `FacturaRepository.kt` the new methods from Task 1 if not already added.

- [ ] **Step 3: Build and commit**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`

```bash
git add app/src/main/java/es/ehfacturas/ui/informes/InformesViewModel.kt \
       app/src/main/java/es/ehfacturas/data/repository/GastoRepository.kt
git commit -m "feat: add InformesViewModel with period filtering and CSV export"
```

---

### Task 8: InformesScreen + Vico chart + BandejaScreen integration

**Files:**
- Create: `app/src/main/java/es/ehfacturas/ui/informes/InformesScreen.kt`
- Modify: `app/src/main/java/es/ehfacturas/ui/bandeja/BandejaScreen.kt`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Vico dependency**

In `app/build.gradle.kts`:
```kotlin
implementation("com.patrykandpatrick.vico:compose-m3:2.1.0")
```

- [ ] **Step 2: Create InformesScreen**

```kotlin
package es.ehfacturas.ui.informes

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.domain.validation.Formateadores

@Composable
fun InformesScreen(
    viewModel: InformesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Compartir CSV
    LaunchedEffect(uiState.csvParaCompartir) {
        uiState.csvParaCompartir?.let { csv ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_TEXT, csv)
                putExtra(Intent.EXTRA_SUBJECT, "Facturas exportadas")
            }
            context.startActivity(Intent.createChooser(intent, "Exportar CSV"))
            viewModel.limpiarCSV()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selector de periodo
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Periodo.entries.forEach { periodo ->
                    FilterChip(
                        selected = uiState.periodo == periodo,
                        onClick = { viewModel.setPeriodo(periodo) },
                        label = { Text(periodo.descripcion) }
                    )
                }
            }
        }

        // Resumen cards (grid 2 columnas)
        item {
            Text("Resumen", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Facturado", uiState.facturado, Color(0xFF2563EB), Modifier.weight(1f))
                StatCard("Cobrado", uiState.cobrado, Color(0xFF16A34A), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Pendiente", uiState.pendiente, Color(0xFFEA580C), Modifier.weight(1f))
                StatCard("IVA repercutido", uiState.ivaRepercutido, Color(0xFF6B7280), Modifier.weight(1f))
            }
        }
        if (uiState.irpfRetenido > 0) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("IRPF retenido", uiState.irpfRetenido, Color(0xFFDC2626), Modifier.weight(1f))
                    StatCard("Beneficio neto", uiState.beneficioNeto, Color(0xFF16A34A), Modifier.weight(1f))
                }
            }
        }
        if (uiState.gastosPeriodo > 0) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Gastos", uiState.gastosPeriodo, Color(0xFFDC2626), Modifier.weight(1f))
                    StatCard("Beneficio real", uiState.beneficioReal, Color(0xFF16A34A), Modifier.weight(1f))
                }
            }
        }
        item {
            Text("${uiState.facturasEmitidas} facturas emitidas en el periodo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Top clientes
        if (uiState.topClientes.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Top clientes", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            items(uiState.topClientes) { (nombre, total) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(nombre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(Formateadores.formatearMoneda(total), style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        // Gastos por categoría
        if (uiState.gastosPorCategoria.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Gastos por categoría", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            items(uiState.gastosPorCategoria) { (categoria, total) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(categoria.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(Formateadores.formatearMoneda(total), style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Exportar
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            OutlinedButton(
                onClick = { viewModel.exportarCSV() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exportar facturas (CSV)")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(titulo: String, valor: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = color)
            Text(Formateadores.formatearMoneda(valor),
                style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
```

- [ ] **Step 3: Replace InformesPlaceholder in BandejaScreen**

In `BandejaScreen.kt`, replace:
```kotlin
4 -> InformesPlaceholder()
```
with:
```kotlin
4 -> InformesScreen()
```

Add import: `import es.ehfacturas.ui.informes.InformesScreen`

Remove the `InformesPlaceholder` composable function entirely.

- [ ] **Step 4: Build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/es/ehfacturas/ui/informes/InformesScreen.kt \
       app/src/main/java/es/ehfacturas/ui/bandeja/BandejaScreen.kt \
       app/build.gradle.kts
git commit -m "feat: add InformesScreen with period reports, top clients, CSV export"
```

---

### Task 9: Full build verification

- [ ] **Step 1: Full build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run all tests**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew :app:testDebugUnitTest 2>&1 | tail -15`
Expected: All PASS

- [ ] **Step 3: Push**

```bash
git push origin main
```
