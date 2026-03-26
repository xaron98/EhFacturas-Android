package es.ehfacturas.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToBandeja: () -> Unit,
    onNavigateToFactura: (Long) -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val mensajes by viewModel.mensajes.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val estadoDetallado by viewModel.estadoDetallado.collectAsStateWithLifecycle()
    val textoManual by viewModel.textoManual.collectAsStateWithLifecycle()
    val estaEscuchando by viewModel.estaEscuchando.collectAsStateWithLifecycle()
    val textoTranscrito by viewModel.textoTranscrito.collectAsStateWithLifecycle()
    val errorSpeech by viewModel.errorSpeech.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher para pedir permiso de micrófono
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleMicrofono()
        }
    }

    // Función que comprueba permiso antes de activar el mic
    val onMicTapConPermiso: () -> Unit = {
        if (estaEscuchando) {
            // Si ya está escuchando, parar directamente
            viewModel.toggleMicrofono()
        } else {
            // Comprobar permiso antes de iniciar
            val tienePermiso = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (tienePermiso) {
                viewModel.toggleMicrofono()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Mostrar errores del reconocimiento de voz
    LaunchedEffect(errorSpeech) {
        errorSpeech?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarErrorSpeech()
        }
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("EhFacturas!") },
                actions = {
                    IconButton(onClick = onNavigateToBandeja) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Bandeja")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Transcripción en tiempo real
                AnimatedVisibility(visible = estaEscuchando && textoTranscrito.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = textoTranscrito,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Indicador de procesamiento
                AnimatedVisibility(visible = estado == EstadoProcesamiento.PROCESANDO) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = estadoDetallado,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                CommandInputBar(
                    textoManual = textoManual,
                    onTextoChange = { viewModel.setTextoManual(it) },
                    onEnviar = { viewModel.enviarTexto(textoManual) },
                    onMicTap = onMicTapConPermiso,
                    estaEscuchando = estaEscuchando,
                    procesando = estado == EstadoProcesamiento.PROCESANDO
                )
            }
        }
    ) { paddingValues ->
        if (mensajes.isEmpty()) {
            // Pantalla de bienvenida
            WelcomeContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                onNavigateToBandeja = onNavigateToBandeja
            )
        } else {
            // Chat
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(mensajes, key = { it.id }) { mensaje ->
                    ChatMessageItem(mensaje = mensaje)
                }
            }
        }
    }
}

@Composable
private fun WelcomeContent(
    modifier: Modifier = Modifier,
    onNavigateToBandeja: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hola! Soy tu asistente de facturación.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Dime qué necesitas: crear una factura, consultar clientes, revisar gastos...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Accesos rápidos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccesoRapidoCard(
                titulo = "Nueva\nfactura",
                modifier = Modifier.weight(1f),
                onClick = { }
            )
            AccesoRapidoCard(
                titulo = "Ver\nfacturas",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToBandeja
            )
            AccesoRapidoCard(
                titulo = "Clientes",
                modifier = Modifier.weight(1f),
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Indicador mic
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pulsa el micrófono o escribe un comando",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccesoRapidoCard(
    titulo: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
