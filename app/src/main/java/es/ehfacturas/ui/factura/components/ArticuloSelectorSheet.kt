// app/src/main/java/es/ehfacturas/ui/factura/components/ArticuloSelectorSheet.kt
package es.ehfacturas.ui.factura.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.ehfacturas.data.db.entity.Articulo
import es.ehfacturas.domain.validation.Formateadores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticuloSelectorSheet(
    articulos: List<Articulo>,
    onSeleccionar: (Articulo) -> Unit,
    onDismiss: () -> Unit
) {
    var filtro by remember { mutableStateOf("") }

    val articulosFiltrados = if (filtro.isEmpty()) {
        articulos
    } else {
        articulos.filter {
            it.nombre.contains(filtro, ignoreCase = true) ||
            it.referencia.contains(filtro, ignoreCase = true)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Seleccionar artículo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                placeholder = { Text("Buscar artículo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(articulosFiltrados, key = { it.id }) { articulo ->
                    ListItem(
                        headlineContent = { Text(articulo.nombre) },
                        supportingContent = {
                            Text("${Formateadores.formatearMoneda(articulo.precioUnitario)} · ${articulo.unidad.abreviatura} · IVA ${articulo.tipoIVA.descripcion}")
                        },
                        modifier = Modifier.clickable {
                            onSeleccionar(articulo)
                            onDismiss()
                        }
                    )
                }

                if (articulosFiltrados.isEmpty()) {
                    item {
                        Text(
                            text = "Sin artículos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
