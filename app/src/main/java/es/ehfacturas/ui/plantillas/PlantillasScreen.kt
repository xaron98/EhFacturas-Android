package es.ehfacturas.ui.plantillas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.PlantillaFactura
import java.text.SimpleDateFormat
import java.util.Locale

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

@Composable
fun PlantillasScreen(
    viewModel: PlantillasViewModel = hiltViewModel()
) {
    val plantillas by viewModel.plantillas.collectAsStateWithLifecycle()
    val resultadoUso by viewModel.resultadoUso.collectAsStateWithLifecycle()

    // Snackbar al usar plantilla
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(resultadoUso) {
        resultadoUso?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarResultado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (plantillas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin plantillas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Las plantillas se crean desde el asistente de voz",
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
                items(plantillas, key = { it.id }) { plantilla ->
                    PlantillaCard(
                        plantilla = plantilla,
                        onUsar = { viewModel.usarPlantilla(plantilla) },
                        onEliminar = { viewModel.eliminar(plantilla) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantillaCard(
    plantilla: PlantillaFactura,
    onUsar: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Card(
        onClick = onUsar,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantilla.nombre,
                    style = MaterialTheme.typography.titleSmall
                )
                if (plantilla.articulosTexto.isNotEmpty()) {
                    Text(
                        text = plantilla.articulosTexto,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Usada ${plantilla.vecesUsada}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatoFecha.format(plantilla.fechaCreacion),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { mostrarConfirmacion = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar plantilla",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar plantilla") },
            text = { Text("Se eliminara la plantilla \"${plantilla.nombre}\". Esta accion no se puede deshacer.") },
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
