package es.ehfacturas.ui.factura.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotosFacturaSheet(
    facturaId: Long,
    fotosRutas: List<String>,
    onFotosChange: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fotos by remember(fotosRutas) { mutableStateOf(fotosRutas.toMutableList()) }
    val bitmaps = remember { mutableStateMapOf<String, Bitmap>() }

    // Cargar bitmaps existentes
    LaunchedEffect(fotos) {
        withContext(Dispatchers.IO) {
            fotos.forEach { ruta ->
                if (!bitmaps.containsKey(ruta)) {
                    val file = File(ruta)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(ruta)?.let { bitmaps[ruta] = it }
                    }
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        scope.launch {
            val nuevasRutas = withContext(Dispatchers.IO) {
                val dir = File(context.filesDir, "fotos/$facturaId")
                dir.mkdirs()
                uris.take(5 - fotos.size).mapNotNull { uri ->
                    comprimirYGuardar(context, uri, dir, fotos.size)
                }
            }
            fotos = (fotos + nuevasRutas).toMutableList()
            onFotosChange(fotos)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Fotos adjuntas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (fotos.isEmpty()) {
                Text("Sin fotos adjuntas", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fotos) { ruta ->
                        Box {
                            bitmaps[ruta]?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto adjunta",
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = {
                                    fotos = fotos.filter { it != ruta }.toMutableList()
                                    bitmaps.remove(ruta)
                                    onFotosChange(fotos)
                                },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (fotos.size < 5) {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir fotos (${fotos.size}/5)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun comprimirYGuardar(context: Context, uri: Uri, dir: File, index: Int): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Redimensionar a max 800px
        val maxDim = 800
        val ratio = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height, 1f)
        val width = (original.width * ratio).toInt()
        val height = (original.height * ratio).toInt()
        val resized = Bitmap.createScaledBitmap(original, width, height, true)
        if (resized != original) original.recycle()

        val archivo = File(dir, "foto_${System.currentTimeMillis()}_$index.jpg")
        FileOutputStream(archivo).use { out ->
            resized.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        resized.recycle()

        archivo.absolutePath
    } catch (_: Exception) {
        null
    }
}
