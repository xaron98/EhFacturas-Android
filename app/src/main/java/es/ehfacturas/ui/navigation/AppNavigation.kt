package es.ehfacturas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import es.ehfacturas.ui.main.MainScreen
import es.ehfacturas.ui.bandeja.BandejaScreen

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
                onNavigateToBandeja = { navController.navigate(Rutas.BANDEJA) }
            )
        }
        composable(Rutas.BANDEJA) {
            BandejaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
