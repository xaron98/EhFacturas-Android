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

        // Gastos por categoria
        if (uiState.gastosPorCategoria.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Gastos por categoria", style = MaterialTheme.typography.titleSmall,
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
