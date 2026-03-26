package es.ehfacturas.ui.recurrentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.FacturaRecurrente
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

private val frecuenciasNombres = mapOf(
    "semanal" to "Semanal",
    "mensual" to "Mensual",
    "trimestral" to "Trimestral",
    "anual" to "Anual"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrentesScreen(
    onBack: () -> Unit = {},
    viewModel: RecurrentesViewModel = hiltViewModel()
) {
    val recurrentes by viewModel.recurrentes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Facturas recurrentes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (recurrentes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventRepeat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin facturas recurrentes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Las recurrentes se crean desde el asistente de voz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recurrentes, key = { it.id }) { recurrente ->
                    RecurrenteCard(
                        recurrente = recurrente,
                        onToggleActivo = { viewModel.toggleActivo(recurrente) },
                        onEliminar = { viewModel.eliminar(recurrente) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecurrenteCard(
    recurrente: FacturaRecurrente,
    onToggleActivo: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    val vencida = recurrente.proximaFecha.before(Date()) && recurrente.activo

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (!recurrente.activo) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (recurrente.activo) Icons.Default.EventRepeat else Icons.Default.EventBusy,
                contentDescription = null,
                tint = when {
                    !recurrente.activo -> MaterialTheme.colorScheme.onSurfaceVariant
                    vencida -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recurrente.nombre,
                    style = MaterialTheme.typography.titleSmall
                )
                if (recurrente.clienteNombre.isNotEmpty()) {
                    Text(
                        text = recurrente.clienteNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatoMoneda.format(recurrente.importeTotal),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = frecuenciasNombres[recurrente.frecuencia] ?: recurrente.frecuencia,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (vencida) "Vencida: " else "Proxima: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vencida) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatoFecha.format(recurrente.proximaFecha),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vencida) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Generada ${recurrente.vecesGenerada}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Switch(
                    checked = recurrente.activo,
                    onCheckedChange = { onToggleActivo() }
                )
                IconButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar recurrente",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar recurrente") },
            text = { Text("Se eliminara la factura recurrente \"${recurrente.nombre}\". Esta accion no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        onEliminar()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
