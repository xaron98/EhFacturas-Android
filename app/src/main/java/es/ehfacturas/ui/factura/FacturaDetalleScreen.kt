// app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleScreen.kt
package es.ehfacturas.ui.factura

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.EstadoFactura
import es.ehfacturas.domain.validation.Formateadores
import es.ehfacturas.ui.factura.components.AccionesRow
import es.ehfacturas.ui.factura.components.TotalesSection
import java.text.SimpleDateFormat
import java.util.Locale

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
private val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturaDetalleScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onNavigateToFactura: (Long) -> Unit,
    viewModel: FacturaDetalleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mensaje snackbar
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    // Navegar a factura duplicada
    LaunchedEffect(uiState.facturaIdNueva) {
        uiState.facturaIdNueva?.let {
            onNavigateToFactura(it)
            viewModel.limpiarNavegacion()
        }
    }

    val factura = uiState.factura

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(factura?.numeroFactura ?: "Factura") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.cargando || factura == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado badge
            val colorEstado = when (factura.estado) {
                EstadoFactura.PRESUPUESTO -> Color(0xFF9333EA)
                EstadoFactura.BORRADOR -> Color(0xFF6B7280)
                EstadoFactura.EMITIDA -> Color(0xFF2563EB)
                EstadoFactura.PAGADA -> Color(0xFF16A34A)
                EstadoFactura.VENCIDA -> Color(0xFFDC2626)
                EstadoFactura.ANULADA -> Color(0xFFEA580C)
            }
            SuggestionChip(
                onClick = {},
                label = { Text(factura.estado.descripcion, style = MaterialTheme.typography.labelLarge) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = colorEstado.copy(alpha = 0.15f),
                    labelColor = colorEstado
                )
            )

            // Acciones
            AccionesRow(
                estado = factura.estado,
                onEditar = { onEdit(factura.id) },
                onEmitir = { viewModel.emitir() },
                onCobrar = { viewModel.cobrar() },
                onAnular = { viewModel.anular() },
                onDuplicar = { viewModel.duplicar() },
                onConvertir = { viewModel.convertirEnFactura() },
                onPdf = { /* Task Fase 4 */ }
            )

            HorizontalDivider()

            // Datos factura
            DetalleRow("Fecha", formatoFecha.format(factura.fecha))
            factura.fechaVencimiento?.let {
                DetalleRow("Vencimiento", formatoFecha.format(it))
            }

            // Cliente
            if (factura.clienteNombre.isNotEmpty()) {
                HorizontalDivider()
                Text("Cliente", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                DetalleRow("Nombre", factura.clienteNombre)
                if (factura.clienteNIF.isNotEmpty()) DetalleRow("NIF", factura.clienteNIF)
                if (factura.clienteDireccion.isNotEmpty()) DetalleRow("Direccion", factura.clienteDireccion)
            }

            HorizontalDivider()

            // Lineas
            Text("Lineas", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            uiState.lineas.forEach { linea ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(linea.concepto, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${Formateadores.formatearDecimal(linea.cantidad)} ${linea.unidad.abreviatura} x ${Formateadores.formatearMoneda(linea.precioUnitario)} · IVA ${Formateadores.formatearDecimal(linea.porcentajeIVA)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            Formateadores.formatearMoneda(linea.calcularSubtotal()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider()

            // Totales
            TotalesSection(
                totales = uiState.totales,
                descuentoGlobalPorcentaje = factura.descuentoGlobalPorcentaje,
                aplicarIRPF = uiState.aplicarIRPF,
                irpfPorcentaje = uiState.irpfPorcentaje
            )

            // Observaciones
            if (factura.observaciones.isNotEmpty()) {
                HorizontalDivider()
                Text("Observaciones", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(factura.observaciones, style = MaterialTheme.typography.bodyMedium)
            }

            if (factura.notasInternas.isNotEmpty()) {
                Text("Notas internas", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(factura.notasInternas, style = MaterialTheme.typography.bodyMedium)
            }

            // Prompt original
            if (!factura.promptOriginal.isNullOrEmpty()) {
                HorizontalDivider()
                Text("Comando de voz original", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(factura.promptOriginal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Registros de facturacion
            if (uiState.registros.isNotEmpty()) {
                HorizontalDivider()
                Text("Registros de facturacion", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                uiState.registros.forEach { registro ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            DetalleRow("Tipo", registro.tipoRegistro.name)
                            DetalleRow("Fecha", formatoFechaHora.format(registro.fechaHoraGeneracion))
                            DetalleRow("Estado", registro.estadoEnvio.descripcion)
                            if (registro.hashRegistro.isNotEmpty()) {
                                DetalleRow("Hash", registro.hashRegistro.take(16) + "...")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetalleRow(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
