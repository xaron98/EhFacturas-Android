package es.ehfacturas.ui.bandeja

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.Gasto
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

private val categoriasNombres = mapOf(
    "material" to "Material",
    "herramientas" to "Herramientas",
    "vehiculo" to "Vehículo",
    "oficina" to "Oficina",
    "otros" to "Otros"
)

private val categoriasIconos = mapOf(
    "material" to Icons.Default.Construction,
    "herramientas" to Icons.Default.Handyman,
    "vehiculo" to Icons.Default.DirectionsCar,
    "oficina" to Icons.Default.Business,
    "otros" to Icons.Default.MoreHoriz
)

@Composable
fun GastosScreen(
    viewModel: GastosViewModel = hiltViewModel()
) {
    val gastosFiltrados by viewModel.gastosFiltrados.collectAsStateWithLifecycle()
    val filtroCategoria by viewModel.filtroCategoria.collectAsStateWithLifecycle()
    val mostrarFormulario by viewModel.mostrarFormulario.collectAsStateWithLifecycle()
    val gastoSeleccionado by viewModel.gastoSeleccionado.collectAsStateWithLifecycle()
    val totalMes by viewModel.totalMes.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total del mes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Gastos este mes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatoMoneda.format(totalMes),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Filtros
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroCategoria == null,
                        onClick = { viewModel.filtrarPorCategoria(null) },
                        label = { Text("Todos") }
                    )
                    Gasto.categoriasGasto.forEach { cat ->
                        FilterChip(
                            selected = filtroCategoria == cat,
                            onClick = { viewModel.filtrarPorCategoria(cat) },
                            label = { Text(categoriasNombres[cat] ?: cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (gastosFiltrados.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Sin gastos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(gastosFiltrados, key = { it.id }) { gasto ->
                    GastoCard(
                        gasto = gasto,
                        onClick = { viewModel.mostrarFormulario(gasto) },
                        onEliminar = { viewModel.eliminarGasto(gasto) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.mostrarFormulario() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo gasto")
        }
    }

    if (mostrarFormulario) {
        GastoFormDialog(
            gasto = gastoSeleccionado,
            onGuardar = { viewModel.guardarGasto(it) },
            onCerrar = { viewModel.cerrarFormulario() }
        )
    }
}

@Composable
private fun GastoCard(
    gasto: Gasto,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = categoriasIconos[gasto.categoria] ?: Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gasto.concepto,
                    style = MaterialTheme.typography.titleSmall
                )
                Row {
                    Text(
                        text = categoriasNombres[gasto.categoria] ?: gasto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (gasto.proveedor.isNotEmpty()) {
                        Text(
                            text = " · ${gasto.proveedor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatoFecha.format(gasto.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatoMoneda.format(gasto.importe),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GastoFormDialog(
    gasto: Gasto?,
    onGuardar: (Gasto) -> Unit,
    onCerrar: () -> Unit
) {
    val esNuevo = gasto == null
    var concepto by remember { mutableStateOf(gasto?.concepto ?: "") }
    var importeTexto by remember { mutableStateOf(gasto?.importe?.let { if (it > 0) it.toString() else "" } ?: "") }
    var categoria by remember { mutableStateOf(gasto?.categoria ?: "otros") }
    var proveedor by remember { mutableStateOf(gasto?.proveedor ?: "") }
    var observaciones by remember { mutableStateOf(gasto?.observaciones ?: "") }
    var deducibleIVA by remember { mutableStateOf(gasto?.deducibleIVA ?: true) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var errorConcepto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (esNuevo) "Nuevo gasto" else "Editar gasto") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = concepto,
                        onValueChange = { concepto = it; errorConcepto = "" },
                        label = { Text("Concepto *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorConcepto.isNotEmpty(),
                        supportingText = if (errorConcepto.isNotEmpty()) {
                            { Text(errorConcepto) }
                        } else null
                    )
                }
                item {
                    OutlinedTextField(
                        value = importeTexto,
                        onValueChange = { importeTexto = it },
                        label = { Text("Importe (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = it }
                    ) {
                        OutlinedTextField(
                            value = categoriasNombres[categoria] ?: categoria,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false }
                        ) {
                            Gasto.categoriasGasto.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(categoriasNombres[cat] ?: cat) },
                                    onClick = { categoria = cat; expandedCategoria = false }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = proveedor,
                        onValueChange = { proveedor = it },
                        label = { Text("Proveedor") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = deducibleIVA,
                            onCheckedChange = { deducibleIVA = it }
                        )
                        Text("IVA deducible")
                    }
                }
                item {
                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validación
                    if (concepto.isBlank()) {
                        errorConcepto = "Concepto requerido"
                        return@Button
                    }

                    val importe = importeTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val nuevoGasto = (gasto ?: Gasto()).copy(
                        concepto = concepto.trim(),
                        importe = importe,
                        categoria = categoria,
                        proveedor = proveedor.trim(),
                        deducibleIVA = deducibleIVA,
                        observaciones = observaciones.trim()
                    )
                    onGuardar(nuevoGasto)
                },
                enabled = true
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}
