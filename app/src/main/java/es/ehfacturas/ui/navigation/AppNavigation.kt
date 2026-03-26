package es.ehfacturas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import es.ehfacturas.ui.bandeja.BandejaScreen
import es.ehfacturas.ui.factura.FacturaDetalleScreen
import es.ehfacturas.ui.factura.FacturaEditScreen
import es.ehfacturas.ui.importador.ImportadorScreen
import es.ehfacturas.ui.main.MainScreen
import es.ehfacturas.ui.plantillas.PlantillasScreen
import es.ehfacturas.ui.recurrentes.RecurrentesScreen
import es.ehfacturas.ui.scanner.ScannerScreen
import es.ehfacturas.ui.settings.AjustesScreen
import es.ehfacturas.ui.subscription.SuscripcionScreen

// Rutas de navegación
object Rutas {
    const val MAIN = "main"
    const val BANDEJA = "bandeja"
    const val FACTURA_DETALLE = "factura/{facturaId}"
    const val FACTURA_EDIT = "factura/edit/{facturaId}"
    const val FACTURA_NUEVA = "factura/nueva"
    const val CLIENTES = "clientes"
    const val ARTICULOS = "articulos"
    const val GASTOS = "gastos"
    const val INFORMES = "informes"
    const val AJUSTES = "ajustes"
    const val IMPORTAR = "importar"
    const val SCANNER = "scanner"
    const val RECURRENTES = "recurrentes"
    const val PLANTILLAS = "plantillas"
    const val SUSCRIPCION = "suscripcion"

    fun facturaDetalle(facturaId: Long) = "factura/$facturaId"
    fun facturaEdit(facturaId: Long) = "factura/edit/$facturaId"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Rutas.MAIN
    ) {
        composable(Rutas.MAIN) {
            MainScreen(
                onNavigateToBandeja = { navController.navigate(Rutas.BANDEJA) },
                onNavigateToFactura = { facturaId ->
                    navController.navigate(Rutas.facturaDetalle(facturaId))
                }
            )
        }
        composable(Rutas.BANDEJA) {
            BandejaScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFactura = { facturaId ->
                    navController.navigate(Rutas.facturaDetalle(facturaId))
                },
                onNavigateToNuevaFactura = {
                    navController.navigate(Rutas.FACTURA_NUEVA)
                },
                onNavigateToAjustes = { navController.navigate(Rutas.AJUSTES) },
                onNavigateToImportar = { navController.navigate(Rutas.IMPORTAR) }
            )
        }
        composable(Rutas.FACTURA_NUEVA) {
            FacturaEditScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Rutas.FACTURA_DETALLE,
            arguments = listOf(navArgument("facturaId") { type = NavType.StringType })
        ) {
            FacturaDetalleScreen(
                onBack = { navController.popBackStack() },
                onEdit = { facturaId ->
                    navController.navigate(Rutas.facturaEdit(facturaId))
                },
                onNavigateToFactura = { facturaId ->
                    navController.navigate(Rutas.facturaDetalle(facturaId)) {
                        popUpTo(Rutas.BANDEJA)
                    }
                },
                onNavigateToRecurrentes = { navController.navigate(Rutas.RECURRENTES) },
                onNavigateToPlantillas = { navController.navigate(Rutas.PLANTILLAS) }
            )
        }
        composable(
            route = Rutas.FACTURA_EDIT,
            arguments = listOf(navArgument("facturaId") { type = NavType.StringType })
        ) {
            FacturaEditScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.AJUSTES) {
            AjustesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.IMPORTAR) {
            ImportadorScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.SCANNER) {
            ScannerScreen(
                onBack = { navController.popBackStack() },
                onTextoEscaneado = { texto ->
                    // Volver a la pantalla anterior con el texto escaneado
                    navController.popBackStack()
                }
            )
        }
        composable(Rutas.RECURRENTES) {
            RecurrentesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.PLANTILLAS) {
            PlantillasScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.SUSCRIPCION) {
            SuscripcionScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
