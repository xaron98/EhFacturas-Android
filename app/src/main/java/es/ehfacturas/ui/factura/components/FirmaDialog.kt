package es.ehfacturas.ui.factura.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun FirmaDialog(
    firmaExistente: String?, // ruta al PNG existente, null si no hay
    onGuardar: (String) -> Unit, // devuelve ruta del PNG guardado
    onDismiss: () -> Unit,
    directorio: File // directorio donde guardar
) {
    val lines = remember { mutableStateListOf<List<Offset>>() }
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var hasDibujo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Firma del cliente") },
        text = {
            Column {
                // Canvas de dibujo
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentLine = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentLine = currentLine + change.position
                                },
                                onDragEnd = {
                                    if (currentLine.size > 1) {
                                        lines.add(currentLine)
                                        hasDibujo = true
                                    }
                                    currentLine = emptyList()
                                }
                            )
                        }
                ) {
                    // Dibujar líneas guardadas
                    val allLines = lines.toList() + listOf(currentLine)
                    allLines.forEach { line ->
                        if (line.size > 1) {
                            val path = Path()
                            path.moveTo(line.first().x, line.first().y)
                            line.drop(1).forEach { point ->
                                path.lineTo(point.x, point.y)
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón limpiar
                TextButton(onClick = {
                    lines.clear()
                    currentLine = emptyList()
                    hasDibujo = false
                }) {
                    Text("Limpiar")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Renderizar a bitmap y guardar
                    val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)

                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        strokeWidth = 4f
                        style = android.graphics.Paint.Style.STROKE
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        isAntiAlias = true
                    }

                    // Escalar de tamaño Compose a 400x200
                    lines.forEach { line ->
                        if (line.size > 1) {
                            val path = android.graphics.Path()
                            path.moveTo(line.first().x, line.first().y)
                            line.drop(1).forEach { point ->
                                path.lineTo(point.x, point.y)
                            }
                            canvas.drawPath(path, paint)
                        }
                    }

                    directorio.mkdirs()
                    val archivo = File(directorio, "firma.png")
                    FileOutputStream(archivo).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    bitmap.recycle()

                    onGuardar(archivo.absolutePath)
                },
                enabled = hasDibujo
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
