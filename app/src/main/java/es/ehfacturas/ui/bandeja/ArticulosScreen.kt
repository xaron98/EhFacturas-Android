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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.Articulo
import es.ehfacturas.data.db.entity.TipoIVA
import es.ehfacturas.data.db.entity.UnidadMedida
import java.text.NumberFormat
import java.util.Locale

private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

@Composable
fun ArticulosScreen(
    viewModel: ArticulosViewModel = hiltViewModel()
) {
    val articulosFiltrados by viewModel.articulosFiltrados.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsStateWithLifecycle()
    val mostrarFormulario by viewModel.mostrarFormulario.collectAsStateWithLifecycle()
    val articuloSeleccionado by viewModel.articuloSeleccionado.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (articulosFiltrados.isEmpty() && categoriaSeleccionada == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Sin artículos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Pulsa + para añadir tu primer artículo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtro por categoría
                if (categorias.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = categoriaSeleccionada == null,
                                onClick = { viewModel.filtrarPorCategoria(null) },
                                label = { Text("Todos") }
                            )
                            categorias.forEach { cat ->
                                FilterChip(
                                    selected = categoriaSeleccionada == cat.id,
                                    onClick = { viewModel.filtrarPorCategoria(cat.id) },
                                    label = { Text(cat.nombre) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(articulosFiltrados, key = { it.id }) { articulo ->
                    ArticuloCard(
                        articulo = articulo,
                        onClick = { viewModel.mostrarFormulario(articulo) },
                        onEliminar = { viewModel.eliminarArticulo(articulo) }
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
            Icon(Icons.Default.Add, contentDescription = "Nuevo artículo")
        }
    }

    if (mostrarFormulario) {
        ArticuloFormDialog(
            articulo = articuloSeleccionado,
            onGuardar = { viewModel.guardarArticulo(it) },
            onCerrar = { viewModel.cerrarFormulario() }
        )
    }
}

@Composable
private fun ArticuloCard(
    articulo: Articulo,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = articulo.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (articulo.referencia.isNotEmpty()) {
                    Text(
                        text = "Ref: ${articulo.referencia}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${articulo.unidad.abreviatura} · IVA ${articulo.tipoIVA.porcentaje.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatoMoneda.format(articulo.precioUnitario),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "con IVA: ${formatoMoneda.format(articulo.precioConIVA)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticuloFormDialog(
    articulo: Articulo?,
    onGuardar: (Articulo) -> Unit,
    onCerrar: () -> Unit
) {
    val esNuevo = articulo == null
    var nombre by remember { mutableStateOf(articulo?.nombre ?: "") }
    var referencia by remember { mutableStateOf(articulo?.referencia ?: "") }
    var descripcion by remember { mutableStateOf(articulo?.descripcion ?: "") }
    var precioTexto by remember { mutableStateOf(articulo?.precioUnitario?.let { if (it > 0) it.toString() else "" } ?: "") }
    var unidad by remember { mutableStateOf(articulo?.unidad ?: UnidadMedida.UNIDAD) }
    var tipoIVA by remember { mutableStateOf(articulo?.tipoIVA ?: TipoIVA.GENERAL) }
    var expandedUnidad by remember { mutableStateOf(false) }
    var expandedIVA by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (esNuevo) "Nuevo artículo" else "Editar artículo") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = referencia,
                        onValueChange = { referencia = it },
                        label = { Text("Referencia") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = precioTexto,
                        onValueChange = { precioTexto = it },
                        label = { Text("Precio unitario (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    // Selector de unidad
                    ExposedDropdownMenuBox(
                        expanded = expandedUnidad,
                        onExpandedChange = { expandedUnidad = it }
                    ) {
                        OutlinedTextField(
                            value = unidad.descripcion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnidad,
                            onDismissRequest = { expandedUnidad = false }
                        ) {
                            UnidadMedida.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.descripcion} (${u.abreviatura})") },
                                    onClick = { unidad = u; expandedUnidad = false }
                                )
                            }
                        }
                    }
                }
                item {
                    // Selector de IVA
                    ExposedDropdownMenuBox(
                        expanded = expandedIVA,
                        onExpandedChange = { expandedIVA = it }
                    ) {
                        OutlinedTextField(
                            value = tipoIVA.descripcion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo IVA") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedIVA) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedIVA,
                            onDismissRequest = { expandedIVA = false }
                        ) {
                            TipoIVA.entries.forEach { iva ->
                                DropdownMenuItem(
                                    text = { Text(iva.descripcion) },
                                    onClick = { tipoIVA = iva; expandedIVA = false }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
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
                    val precio = precioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val nuevoArticulo = (articulo ?: Articulo()).copy(
                        nombre = nombre.trim(),
                        referencia = referencia.trim(),
                        descripcion = descripcion.trim(),
                        precioUnitario = precio,
                        unidad = unidad,
                        tipoIVA = tipoIVA
                    )
                    onGuardar(nuevoArticulo)
                },
                enabled = nombre.isNotBlank()
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
