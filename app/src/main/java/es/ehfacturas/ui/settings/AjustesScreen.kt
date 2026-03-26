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
                            errorNif = "NIF/CIF no valido"
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
                    label = { Text("Nombre / Razon social") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
                    label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            }
            item {
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            }

            // Direccion
            item {
                Text("Direccion", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedTextField(value = direccion, onValueChange = { direccion = it },
                    label = { Text("Direccion") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
                    Text("Aplicar retencion IRPF", modifier = Modifier.weight(1f))
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
                    Text("7% los primeros 3 anos de actividad, 15% a partir del 4o.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Numeracion
            item {
                Text("Numeracion de facturas", style = MaterialTheme.typography.titleSmall,
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
