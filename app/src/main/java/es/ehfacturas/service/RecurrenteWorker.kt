package es.ehfacturas.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import es.ehfacturas.ai.ToolExecutor
import es.ehfacturas.data.db.dao.FacturaRecurrenteDao
import es.ehfacturas.data.db.entity.FacturaRecurrente
import java.util.Date
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurrenteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val facturaRecurrenteDao: FacturaRecurrenteDao,
    private val toolExecutor: ToolExecutor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pendientes = facturaRecurrenteDao.obtenerPendientes(Date())
            Log.d(TAG, "Comprobando recurrentes: ${pendientes.size} pendientes")

            pendientes.forEach { rec ->
                if (rec.activo) {
                    Log.d(TAG, "Generando factura recurrente: ${rec.nombre}")
                    toolExecutor.executeTool(
                        "crear_factura",
                        mapOf(
                            "nombreCliente" to rec.clienteNombre,
                            "articulosTexto" to rec.articulosTexto
                        )
                    )
                    val siguiente = FacturaRecurrente.calcularProximaFecha(
                        rec.proximaFecha, rec.frecuencia
                    )
                    facturaRecurrenteDao.actualizar(
                        rec.copy(
                            proximaFecha = siguiente,
                            vecesGenerada = rec.vecesGenerada + 1
                        )
                    )
                    Log.d(TAG, "Factura generada. Proxima: $siguiente")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en RecurrenteWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "RecurrenteWorker"
        private const val WORK_NAME = "recurrente_check"

        /**
         * Programa la comprobacion diaria de facturas recurrentes.
         * Se ejecuta una vez al dia, manteniendo la programacion tras reinicios.
         */
        fun programar(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecurrenteWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
