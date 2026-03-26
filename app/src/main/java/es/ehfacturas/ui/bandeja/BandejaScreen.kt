package es.ehfacturas.ui.bandeja

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.ehfacturas.ui.informes.InformesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFactura: (Long) -> Unit = {},
    onNavigateToNuevaFactura: () -> Unit = {},
    onNavigateToAjustes: () -> Unit = {}
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val tabs = listOf("Facturas", "Clientes", "Artículos", "Gastos", "Informes")
    val iconos = listOf(
        Icons.Default.Receipt,
        Icons.Default.People,
        Icons.Default.Inventory2,
        Icons.Default.Payments,
        Icons.Default.BarChart
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandeja") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAjustes) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = tabSeleccionada,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        text = { Text(titulo) },
                        icon = { Icon(iconos[index], contentDescription = titulo) }
                    )
                }
            }

            // Contenido según tab
            when (tabSeleccionada) {
                0 -> FacturasScreen(
                    onFacturaClick = onNavigateToFactura,
                    onNuevaFactura = onNavigateToNuevaFactura
                )
                1 -> ClientesScreen()
                2 -> ArticulosScreen()
                3 -> GastosScreen()
                4 -> InformesScreen()
            }
        }
    }
}

