package es.ehfacturas.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.ehfacturas.data.db.AppDatabase
import es.ehfacturas.data.db.DatabaseCallback
import es.ehfacturas.data.db.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ehfacturas.db"
        )
            .addCallback(DatabaseCallback())
            .build()
    }

    @Provides fun provideNegocioDao(db: AppDatabase): NegocioDao = db.negocioDao()
    @Provides fun provideClienteDao(db: AppDatabase): ClienteDao = db.clienteDao()
    @Provides fun provideCategoriaDao(db: AppDatabase): CategoriaDao = db.categoriaDao()
    @Provides fun provideArticuloDao(db: AppDatabase): ArticuloDao = db.articuloDao()
    @Provides fun provideFacturaDao(db: AppDatabase): FacturaDao = db.facturaDao()
    @Provides fun provideLineaFacturaDao(db: AppDatabase): LineaFacturaDao = db.lineaFacturaDao()
    @Provides fun provideGastoDao(db: AppDatabase): GastoDao = db.gastoDao()
    @Provides fun provideRegistroFacturacionDao(db: AppDatabase): RegistroFacturacionDao = db.registroFacturacionDao()
    @Provides fun provideEventoSIFDao(db: AppDatabase): EventoSIFDao = db.eventoSIFDao()
    @Provides fun providePlantillaDao(db: AppDatabase): PlantillaDao = db.plantillaDao()
    @Provides fun provideFacturaRecurrenteDao(db: AppDatabase): FacturaRecurrenteDao = db.facturaRecurrenteDao()
    @Provides fun providePerfilImportacionDao(db: AppDatabase): PerfilImportacionDao = db.perfilImportacionDao()
}
