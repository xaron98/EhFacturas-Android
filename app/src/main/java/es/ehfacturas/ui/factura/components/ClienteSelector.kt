// app/src/main/java/es/ehfacturas/ui/factura/components/ClienteSelector.kt
package es.ehfacturas.ui.factura.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.ehfacturas.data.db.entity.Cliente

@Composable
fun ClienteSelector(
    busquedaTexto: String,
    clienteSeleccionado: String,
    sugerencias: List<Cliente>,
    onBuscar: (String) -> Unit,
    onSeleccionar: (Cliente) -> Unit,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = if (clienteSeleccionado.isNotEmpty()) clienteSeleccionado else busquedaTexto,
            onValueChange = { onBuscar(it) },
            label = { Text("Cliente") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            trailingIcon = {
                if (clienteSeleccionado.isNotEmpty()) {
                    IconButton(onClick = onLimpiar) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            readOnly = clienteSeleccionado.isNotEmpty()
        )

        // Lista de sugerencias
        if (sugerencias.isNotEmpty() && clienteSeleccionado.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column {
                    sugerencias.take(5).forEach { cliente ->
                        ListItem(
                            headlineContent = { Text(cliente.nombre) },
                            supportingContent = {
                                if (cliente.nif.isNotEmpty()) Text("NIF: ${cliente.nif}")
                            },
                            modifier = Modifier.clickable { onSeleccionar(cliente) }
                        )
                    }
                }
            }
        }
    }
}
