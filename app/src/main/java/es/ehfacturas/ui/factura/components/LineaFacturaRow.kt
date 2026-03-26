// app/src/main/java/es/ehfacturas/ui/factura/components/LineaFacturaRow.kt
package es.ehfacturas.ui.factura.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.ehfacturas.data.db.entity.LineaFactura
import es.ehfacturas.data.db.entity.TipoIVA
import es.ehfacturas.data.db.entity.UnidadMedida
import es.ehfacturas.domain.validation.Formateadores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineaFacturaRow(
    index: Int,
    linea: LineaFactura,
    errores: Map<String, String>,
    onUpdate: (LineaFactura) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cantidadTexto by remember(linea.id, linea.cantidad) {
        mutableStateOf(if (linea.cantidad == 0.0) "" else Formateadores.formatearDecimal(linea.cantidad))
    }
    var precioTexto by remember(linea.id, linea.precioUnitario) {
        mutableStateOf(if (linea.precioUnitario == 0.0) "" else Formateadores.formatearDecimal(linea.precioUnitario))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila 1: Concepto + eliminar
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = linea.concepto,
                    onValueChange = { onUpdate(linea.copy(concepto = it)) },
                    label = { Text("Concepto") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = errores.containsKey("linea_${index}_concepto"),
                    supportingText = errores["linea_${index}_concepto"]?.let { { Text(it) } }
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar línea",
                        tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 2: Cantidad + Precio + Unidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cantidadTexto,
                    onValueChange = { text ->
                        cantidadTexto = text
                        Formateadores.parsearPrecio(text)?.let { valor ->
                            onUpdate(linea.copy(cantidad = valor))
                        }
                    },
                    label = { Text("Cant.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errores.containsKey("linea_${index}_cantidad")
                )
                OutlinedTextField(
                    value = precioTexto,
                    onValueChange = { text ->
                        precioTexto = text
                        Formateadores.parsearPrecio(text)?.let { valor ->
                            onUpdate(linea.copy(precioUnitario = valor))
                        }
                    },
                    label = { Text("Precio") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("€") },
                    isError = errores.containsKey("linea_${index}_precio")
                )

                // Dropdown unidad
                var expandedUnidad by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedUnidad,
                    onExpandedChange = { expandedUnidad = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = linea.unidad.abreviatura,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ud.") },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnidad,
                        onDismissRequest = { expandedUnidad = false }
                    ) {
                        UnidadMedida.entries.forEach { unidad ->
                            DropdownMenuItem(
                                text = { Text("${unidad.abreviatura} - ${unidad.descripcion}") },
                                onClick = {
                                    onUpdate(linea.copy(unidad = unidad))
                                    expandedUnidad = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 3: IVA + Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown IVA
                var expandedIVA by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedIVA,
                    onExpandedChange = { expandedIVA = it },
                    modifier = Modifier.width(160.dp)
                ) {
                    OutlinedTextField(
                        value = TipoIVA.entries.find { it.porcentaje == linea.porcentajeIVA }?.descripcion ?: "IVA ${linea.porcentajeIVA}%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("IVA") },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedIVA,
                        onDismissRequest = { expandedIVA = false }
                    ) {
                        TipoIVA.entries.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.descripcion) },
                                onClick = {
                                    onUpdate(linea.copy(porcentajeIVA = tipo.porcentaje))
                                    expandedIVA = false
                                }
                            )
                        }
                    }
                }

                // Subtotal
                Text(
                    text = "Subtotal: ${Formateadores.formatearMoneda(linea.calcularSubtotal())}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
