package es.ehfacturas.ui.bandeja

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.data.db.entity.Cliente
import es.ehfacturas.domain.validation.NifValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val mostrarFormulario by viewModel.mostrarFormulario.collectAsStateWithLifecycle()
    val clienteSeleccionado by viewModel.clienteSeleccionado.collectAsStateWithLifecycle()
    var textoBusqueda by remember { mutableStateOf("") }

    val clientesMostrados = if (textoBusqueda.isNotEmpty()) {
        clientes.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
            it.nif.contains(textoBusqueda, ignoreCase = true)
        }
    } else clientes

    Box(modifier = Modifier.fillMaxSize()) {
        if (clientes.isEmpty()) {
            // Estado vacío
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Sin clientes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Pulsa + para añadir tu primer cliente",
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
                // Barra de búsqueda
                item {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar cliente...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { textoBusqueda = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(clientesMostrados, key = { it.id }) { cliente ->
                    ClienteCard(
                        cliente = cliente,
                        onClick = { viewModel.mostrarFormulario(cliente) },
                        onEliminar = { viewModel.eliminarCliente(cliente) }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { viewModel.mostrarFormulario() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo cliente")
        }
    }

    // Dialog formulario
    if (mostrarFormulario) {
        ClienteFormDialog(
            cliente = clienteSeleccionado,
            onGuardar = { viewModel.guardarCliente(it) },
            onCerrar = { viewModel.cerrarFormulario() }
        )
    }
}

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }

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
            // Avatar con iniciales
            Surface(
                modifier = Modifier.size(48.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = cliente.iniciales,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cliente.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (cliente.nif.isNotEmpty()) {
                    Text(
                        text = cliente.nif,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (cliente.email.isNotEmpty()) {
                    Text(
                        text = cliente.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { mostrarMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = { mostrarMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = { mostrarMenu = false; onClick() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = { mostrarMenu = false; onEliminar() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClienteFormDialog(
    cliente: Cliente?,
    onGuardar: (Cliente) -> Unit,
    onCerrar: () -> Unit
) {
    val esNuevo = cliente == null
    var nombre by remember { mutableStateOf(cliente?.nombre ?: "") }
    var nif by remember { mutableStateOf(cliente?.nif ?: "") }
    var direccion by remember { mutableStateOf(cliente?.direccion ?: "") }
    var codigoPostal by remember { mutableStateOf(cliente?.codigoPostal ?: "") }
    var ciudad by remember { mutableStateOf(cliente?.ciudad ?: "") }
    var provincia by remember { mutableStateOf(cliente?.provincia ?: "") }
    var telefono by remember { mutableStateOf(cliente?.telefono ?: "") }
    var email by remember { mutableStateOf(cliente?.email ?: "") }
    var observaciones by remember { mutableStateOf(cliente?.observaciones ?: "") }
    var errorNif by remember { mutableStateOf("") }
    var errorEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (esNuevo) "Nuevo cliente" else "Editar cliente") },
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
                        value = nif,
                        onValueChange = { nif = it; errorNif = "" },
                        label = { Text("NIF/CIF") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorNif.isNotEmpty(),
                        supportingText = if (errorNif.isNotEmpty()) {
                            { Text(errorNif) }
                        } else null
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorEmail = "" },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorEmail.isNotEmpty(),
                        supportingText = if (errorEmail.isNotEmpty()) {
                            { Text(errorEmail) }
                        } else null
                    )
                }
                item {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = codigoPostal,
                            onValueChange = { codigoPostal = it },
                            label = { Text("C.P.") },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ciudad,
                            onValueChange = { ciudad = it },
                            label = { Text("Ciudad") },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = provincia,
                        onValueChange = { provincia = it },
                        label = { Text("Provincia") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
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
                    var hayErrores = false
                    if (!NifValidator.esValido(nif.trim())) {
                        errorNif = "NIF/CIF/NIE no válido"
                        hayErrores = true
                    }
                    if (!NifValidator.esEmailValido(email.trim())) {
                        errorEmail = "Email no válido"
                        hayErrores = true
                    }
                    if (hayErrores) return@Button

                    val nuevoCliente = (cliente ?: Cliente()).copy(
                        nombre = nombre.trim(),
                        nif = nif.trim(),
                        direccion = direccion.trim(),
                        codigoPostal = codigoPostal.trim(),
                        ciudad = ciudad.trim(),
                        provincia = provincia.trim(),
                        telefono = telefono.trim(),
                        email = email.trim(),
                        observaciones = observaciones.trim()
                    )
                    onGuardar(nuevoCliente)
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
