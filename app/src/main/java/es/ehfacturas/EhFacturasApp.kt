package es.ehfacturas

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import es.ehfacturas.service.RecurrenteWorker
import javax.inject.Inject

@HiltAndroidApp
class EhFacturasApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Programar comprobacion diaria de facturas recurrentes
        RecurrenteWorker.programar(this)
    }
}
