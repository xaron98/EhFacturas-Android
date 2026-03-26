// app/src/main/java/es/ehfacturas/ui/factura/FacturaEditScreen.kt
package es.ehfacturas.ui.factura

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.TipoFacturaVF
import es.ehfacturas.domain.validation.Formateadores
import es.ehfacturas.ui.factura.components.*
import java.text.SimpleDateFormat
import java.util.*

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturaEditScreen(
    onBack: () -> Unit,
    viewModel: FacturaEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busquedaCliente by viewModel.busquedaCliente.collectAsStateWithLifecycle()
    val clientesSugeridos by viewModel.clientesSugeridos.collectAsStateWithLifecycle()
    val articulos by viewModel.articulos.collectAsStateWithLifecycle()

    var mostrarSelectorArticulo by remember { mutableStateOf(false) }
    var mostrarDatePickerFecha by remember { mutableStateOf(false) }
    var mostrarDatePickerVencimiento by remember { mutableStateOf(false) }

    // Navegar atrás al guardar
    LaunchedEffect(uiState.guardadoOk) {
        if (uiState.guardadoOk) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.esNueva) "Nueva factura"
                        else "Factura ${uiState.factura.numeroFactura}"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.guardar() },
                        enabled = !uiState.guardando
                    ) {
                        if (uiState.guardando) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- CABECERA ---
            Text("Cabecera", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            // Número de factura
            OutlinedTextField(
                value = uiState.factura.numeroFactura,
                onValueChange = {},
                label = { Text("Número de factura") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false
            )

            // Cliente
            ClienteSelector(
                busquedaTexto = busquedaCliente,
                clienteSeleccionado = uiState.factura.clienteNombre,
                sugerencias = clientesSugeridos,
                onBuscar = { viewModel.buscarClientes(it) },
                onSeleccionar = { viewModel.setCliente(it) },
                onLimpiar = { viewModel.limpiarCliente() }
            )

            // Fechas
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

            // Tipo factura
            var expandedTipo by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = it }
            ) {
                OutlinedTextField(
                    value = when (uiState.factura.tipoFactura) {
                        TipoFacturaVF.COMPLETA -> "Completa"
                        TipoFacturaVF.SIMPLIFICADA -> "Simplificada"
                        TipoFacturaVF.RECTIFICATIVA -> "Rectificativa"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de factura") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                    TipoFacturaVF.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.setTipoFactura(tipo)
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- LÍNEAS ---
            Text("Líneas de factura", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            if (uiState.errores.containsKey("lineas")) {
                Text(
                    text = uiState.errores["lineas"]!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            uiState.lineas.forEachIndexed { index, linea ->
                LineaFacturaRow(
                    index = index,
                    linea = linea,
                    errores = uiState.errores,
                    onUpdate = { viewModel.updateLinea(index, it) },
                    onDelete = { viewModel.removeLinea(index) }
                )
            }

            // Botones añadir línea
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.addLinea() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir línea")
                }
                OutlinedButton(
                    onClick = { mostrarSelectorArticulo = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Desde artículo")
                }
            }

            HorizontalDivider()

            // --- TOTALES ---
            // Descuento global
            var descuentoTexto by remember(uiState.factura.descuentoGlobalPorcentaje) {
                mutableStateOf(
                    if (uiState.factura.descuentoGlobalPorcentaje == 0.0) ""
                    else Formateadores.formatearDecimal(uiState.factura.descuentoGlobalPorcentaje)
                )
            }
            OutlinedTextField(
                value = descuentoTexto,
                onValueChange = { text ->
                    descuentoTexto = text
                    val valor = Formateadores.parsearPrecio(text) ?: 0.0
                    viewModel.setDescuentoGlobal(valor.coerceIn(0.0, 100.0))
                },
                label = { Text("Descuento global (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("%") }
            )

            TotalesSection(
                totales = uiState.totales,
                descuentoGlobalPorcentaje = uiState.factura.descuentoGlobalPorcentaje,
                aplicarIRPF = uiState.aplicarIRPF,
                irpfPorcentaje = uiState.irpfPorcentaje
            )

            HorizontalDivider()

            // --- OBSERVACIONES ---
            OutlinedTextField(
                value = uiState.factura.observaciones,
                onValueChange = { viewModel.setObservaciones(it) },
                label = { Text("Observaciones (visible en factura)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = uiState.factura.notasInternas,
                onValueChange = { viewModel.setNotasInternas(it) },
                label = { Text("Notas internas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
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

    // BottomSheet de artículos
    if (mostrarSelectorArticulo) {
        ArticuloSelectorSheet(
            articulos = articulos,
            onSeleccionar = { viewModel.addLineaDesdeArticulo(it) },
            onDismiss = { mostrarSelectorArticulo = false }
        )
    }
}
