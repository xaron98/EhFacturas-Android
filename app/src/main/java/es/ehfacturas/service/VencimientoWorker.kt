package es.ehfacturas.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import es.ehfacturas.data.db.dao.FacturaDao
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class VencimientoWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val facturaDao: FacturaDao
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "vencimientos"
        const val WORK_NAME = "vencimiento_check"

        fun programar(context: Context) {
            val request = PeriodicWorkRequestBuilder<VencimientoWorker>(4, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        crearCanal()

        val hoy = Date()
        val cal = Calendar.getInstance()

        // Facturas que vencen hoy
        val vencenHoy = facturaDao.obtenerVencidas(hoy)
        vencenHoy.forEach { factura ->
            enviarNotificacion(
                id = "hoy-${factura.id}".hashCode(),
                titulo = "Factura vence hoy",
                texto = "${factura.numeroFactura} de ${factura.clienteNombre} vence hoy (${String.format("%.2f", factura.totalFactura)} \u20AC)"
            )
        }

        // Facturas que vencen en 3 dias
        cal.time = hoy
        cal.add(Calendar.DAY_OF_MONTH, 3)
        val en3Dias = cal.time

        cal.time = hoy
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val manana = cal.time

        val vencenPronto = facturaDao.facturasVencenEntre(manana, en3Dias)
        vencenPronto.forEach { factura ->
            enviarNotificacion(
                id = "3d-${factura.id}".hashCode(),
                titulo = "Factura por vencer",
                texto = "${factura.numeroFactura} de ${factura.clienteNombre} vence en 3 d\u00EDas (${String.format("%.2f", factura.totalFactura)} \u20AC)"
            )
        }

        return Result.success()
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vencimientos de facturas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de facturas pr\u00F3ximas a vencer"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun enviarNotificacion(id: Int, titulo: String, texto: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }
}
