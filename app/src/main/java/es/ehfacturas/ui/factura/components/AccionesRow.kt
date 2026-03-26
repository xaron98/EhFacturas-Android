// app/src/main/java/es/ehfacturas/ui/factura/components/AccionesRow.kt
package es.ehfacturas.ui.factura.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.ehfacturas.data.db.entity.EstadoFactura

@Composable
fun AccionesRow(
    estado: EstadoFactura,
    onEditar: () -> Unit,
    onEmitir: () -> Unit,
    onCobrar: () -> Unit,
    onAnular: () -> Unit,
    onDuplicar: () -> Unit,
    onConvertir: () -> Unit,
    onPdf: () -> Unit,
    onFotos: () -> Unit = {},
    onFirma: () -> Unit = {},
    onRecurrente: () -> Unit = {},
    onPlantilla: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Editar solo en borrador/presupuesto
        if (estado == EstadoFactura.BORRADOR || estado == EstadoFactura.PRESUPUESTO) {
            AccionButton("Editar", Icons.Default.Edit, onClick = onEditar)
        }

        // Convertir presupuesto en factura
        if (estado == EstadoFactura.PRESUPUESTO) {
            AccionButton("Convertir", Icons.Default.Transform, onClick = onConvertir)
        }

        // Emitir solo borrador
        if (estado == EstadoFactura.BORRADOR) {
            AccionButton("Emitir", Icons.AutoMirrored.Filled.Send, onClick = onEmitir)
        }

        // Cobrar solo emitida
        if (estado == EstadoFactura.EMITIDA) {
            AccionButton("Cobrar", Icons.Default.AttachMoney, onClick = onCobrar)
        }

        // PDF siempre visible
        AccionButton("PDF", Icons.Default.PictureAsPdf, onClick = onPdf)

        // Duplicar siempre
        AccionButton("Duplicar", Icons.Default.ContentCopy, onClick = onDuplicar)

        // Fotos adjuntas
        AccionButton("Fotos", Icons.Default.PhotoCamera, onClick = onFotos)

        // Firma del cliente
        AccionButton("Firma", Icons.Default.Draw, onClick = onFirma)

        // Crear recurrente desde esta factura
        AccionButton("Recurrente", Icons.Default.EventRepeat, onClick = onRecurrente)

        // Guardar como plantilla
        AccionButton("Plantilla", Icons.Default.Description, onClick = onPlantilla)

        // Anular solo emitida/pagada
        if (estado == EstadoFactura.EMITIDA || estado == EstadoFactura.PAGADA) {
            AccionButton("Anular", Icons.Default.Cancel, onClick = onAnular)
        }
    }
}

@Composable
private fun AccionButton(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    FilledTonalButton(onClick = onClick) {
        Icon(icono, contentDescription = texto, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto)
    }
}
