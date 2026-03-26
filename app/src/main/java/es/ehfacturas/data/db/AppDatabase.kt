package es.ehfacturas.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.ehfacturas.data.db.converter.Converters
import es.ehfacturas.data.db.dao.*
import es.ehfacturas.data.db.entity.*

@Database(
    entities = [
        Negocio::class,
        Cliente::class,
        Categoria::class,
        Articulo::class,
        Factura::class,
        LineaFactura::class,
        Gasto::class,
        RegistroFacturacion::class,
        EventoSIF::class,
        FacturaRecurrente::class,
        PlantillaFactura::class,
        PerfilImportacion::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun negocioDao(): NegocioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun articuloDao(): ArticuloDao
    abstract fun facturaDao(): FacturaDao
    abstract fun lineaFacturaDao(): LineaFacturaDao
    abstract fun gastoDao(): GastoDao
    abstract fun registroFacturacionDao(): RegistroFacturacionDao
    abstract fun eventoSIFDao(): EventoSIFDao
    abstract fun plantillaDao(): PlantillaDao
    abstract fun facturaRecurrenteDao(): FacturaRecurrenteDao
    abstract fun perfilImportacionDao(): PerfilImportacionDao
}
