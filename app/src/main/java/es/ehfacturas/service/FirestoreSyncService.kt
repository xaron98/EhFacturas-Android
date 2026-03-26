package es.ehfacturas.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import es.ehfacturas.data.db.dao.*
import java.util.concurrent.TimeUnit

/**
 * Servicio de sincronización con Firebase Firestore.
 *
 * CONFIGURACIÓN NECESARIA:
 * 1. Crear proyecto en Firebase Console
 * 2. Añadir google-services.json a app/
 * 3. Descomentar dependencias en build.gradle.kts
 * 4. Descomentar el código de sync en doWork()
 *
 * Datos sincronizados: Negocio, Clientes, Artículos, Facturas, Gastos
 */
@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val negocioDao: NegocioDao,
    private val clienteDao: ClienteDao,
    private val articuloDao: ArticuloDao,
    private val facturaDao: FacturaDao,
    private val gastoDao: GastoDao
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "firestore_sync"

        fun programar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<FirestoreSyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        // TODO: Implementar cuando se configure Firebase
        //
        // val firestore = Firebase.firestore
        // val userId = Firebase.auth.currentUser?.uid ?: return Result.failure()
        // val userDoc = firestore.collection("usuarios").document(userId)
        //
        // // Sync negocio
        // val negocio = negocioDao.obtenerNegocioSync()
        // negocio?.let { userDoc.collection("negocio").document("config").set(it.toMap()) }
        //
        // // Sync clientes
        // val clientes = clienteDao.obtenerTodosSync()
        // clientes.forEach { cliente ->
        //     userDoc.collection("clientes").document(cliente.id.toString()).set(cliente.toMap())
        // }
        //
        // // Sync facturas
        // val facturas = facturaDao.obtenerTodasSync()
        // facturas.forEach { factura ->
        //     userDoc.collection("facturas").document(factura.id.toString()).set(factura.toMap())
        // }
        //
        // // Sync gastos
        // val gastos = gastoDao.obtenerTodosSync()
        // gastos.forEach { gasto ->
        //     userDoc.collection("gastos").document(gasto.id.toString()).set(gasto.toMap())
        // }

        return Result.success()
    }
}
