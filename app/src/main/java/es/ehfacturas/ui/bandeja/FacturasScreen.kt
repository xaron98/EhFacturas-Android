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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.EstadoFactura
import es.ehfacturas.data.db.entity.Factura
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

@Composable
fun FacturasScreen(
    onFacturaClick: (Long) -> Unit = {},
    onNuevaFactura: () -> Unit = {},
    viewModel: FacturasViewModel = hiltViewModel()
) {
    val facturasFiltradas by viewModel.facturasFiltradas.collectAsStateWithLifecycle()
    val filtroEstado by viewModel.filtroEstado.collectAsStateWithLifecycle()
    val facturacionMes by viewModel.facturacionMes.collectAsStateWithLifecycle()
    val totalFacturas by viewModel.totalFacturas.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dashboard cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        titulo = "Este mes",
                        valor = formatoMoneda.format(facturacionMes),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        titulo = "Total",
                        valor = "$totalFacturas",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        titulo = "Pendientes",
                        valor = "$pendientes",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filtros por estado
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroEstado == null,
                        onClick = { viewModel.filtrarPorEstado(null) },
                        label = { Text("Todas") }
                    )
                    EstadoFactura.entries.forEach { estado ->
                        FilterChip(
                            selected = filtroEstado == estado,
                            onClick = { viewModel.filtrarPorEstado(estado) },
                            label = { Text(estado.descripcion) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (facturasFiltradas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Sin facturas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(facturasFiltradas, key = { it.id }) { factura ->
                    FacturaCard(
                        factura = factura,
                        onClick = { onFacturaClick(factura.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onNuevaFactura,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva factura")
        }
    }
}

@Composable
private fun StatCard(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
private fun FacturaCard(factura: Factura, onClick: () -> Unit) {
    val colorEstado = when (factura.estado) {
        EstadoFactura.PRESUPUESTO -> Color(0xFF9333EA)
        EstadoFactura.BORRADOR -> Color(0xFF6B7280)
        EstadoFactura.EMITIDA -> Color(0xFF2563EB)
        EstadoFactura.PAGADA -> Color(0xFF16A34A)
        EstadoFactura.VENCIDA -> Color(0xFFDC2626)
        EstadoFactura.ANULADA -> Color(0xFFEA580C)
    }

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = factura.numeroFactura.ifEmpty { "Sin número" },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                factura.estado.descripcion,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = colorEstado.copy(alpha = 0.15f),
                            labelColor = colorEstado
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
                Text(
                    text = factura.clienteNombre.ifEmpty { "Sin cliente" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatoFecha.format(factura.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatoMoneda.format(factura.totalFactura),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
