package es.ehfacturas.ui.importador

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.ehfacturas.domain.importador.ResultadoImportacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportadorScreen(
    onBack: () -> Unit,
    viewModel: ImportadorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.cargarCSV(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar ${if (uiState.tipo == "articulos") "Articulos" else "Clientes"}") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.paso == 0) onBack()
                        else viewModel.reiniciar()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.paso) {
                0 -> PasoSeleccion(
                    tipo = uiState.tipo,
                    onTipoChange = { viewModel.setTipo(it) },
                    onSeleccionar = { csvLauncher.launch("text/*") },
                    error = uiState.error
                )
                1 -> PasoPreview(
                    uiState = uiState,
                    onMapeoChange = { campo, indice -> viewModel.actualizarMapeo(campo, indice) },
                    onImportar = { viewModel.importar() }
                )
                2 -> PasoImportando()
                3 -> PasoResultado(
                    resultado = uiState.resultado,
                    onVolver = onBack
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasoSeleccion(
    tipo: String,
    onTipoChange: (String) -> Unit,
    onSeleccionar: () -> Unit,
    error: String?
) {
    Spacer(modifier = Modifier.height(32.dp))

    Text("Tipo de importacion", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = tipo == "articulos",
            onClick = { onTipoChange("articulos") },
            shape = SegmentedButtonDefaults.itemShape(0, 2)
        ) { Text("Articulos") }
        SegmentedButton(
            selected = tipo == "clientes",
            onClick = { onTipoChange("clientes") },
            shape = SegmentedButtonDefaults.itemShape(1, 2)
        ) { Text("Clientes") }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = onSeleccionar, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.UploadFile, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Seleccionar archivo CSV")
    }

    error?.let {
        Spacer(modifier = Modifier.height(12.dp))
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ColumnScope.PasoPreview(
    uiState: ImportadorUiState,
    onMapeoChange: (String, Int) -> Unit,
    onImportar: () -> Unit
) {
    val csv = uiState.csv ?: return

    Text("Preview", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    Text(
        "Separador: '${if (csv.separador == '\t') "TAB" else csv.separador}'  |  Encoding: ${csv.encoding}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        "${csv.filas.size} filas detectadas  |  Confianza: ${(uiState.confianza * 100).toInt()}%",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text("Mapeo de columnas:", style = MaterialTheme.typography.titleSmall)

    LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(uiState.mapeo.entries.toList()) { (campo, indice) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(campo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    "-> ${csv.cabeceras.getOrElse(indice) { "?" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Preview primeras 3 filas
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { Text("Primeras filas:", style = MaterialTheme.typography.titleSmall) }
        items(csv.filas.take(3)) { fila ->
            Text(
                fila.joinToString(" | ").take(100),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onImportar, modifier = Modifier.fillMaxWidth()) {
        Text("Importar ${uiState.csv?.filas?.size ?: 0} registros")
    }
}

@Composable
private fun PasoImportando() {
    Spacer(modifier = Modifier.height(48.dp))
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(16.dp))
    Text("Importando...", style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun PasoResultado(
    resultado: ResultadoImportacion?,
    onVolver: () -> Unit
) {
    resultado ?: return

    Spacer(modifier = Modifier.height(32.dp))
    Icon(
        Icons.Default.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("Importacion completada", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Importados: ${resultado.importados}", style = MaterialTheme.typography.bodyLarge)
            Text("Duplicados: ${resultado.duplicados}", style = MaterialTheme.typography.bodyMedium)
            Text("Errores: ${resultado.errores}", style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (resultado.mensajes.size > 1) {
        Spacer(modifier = Modifier.height(8.dp))
        resultado.mensajes.drop(1).forEach { msg ->
            Text(
                msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
        Text("Volver")
    }
}
