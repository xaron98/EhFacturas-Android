package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.PlantillaFactura
import es.ehfacturas.data.db.entity.FacturaRecurrente
import es.ehfacturas.data.db.entity.PerfilImportacion
import kotlinx.coroutines.flow.Flow
import java.util.Date

// DAO para PlantillaFactura
@Dao
interface PlantillaDao {
    @Query("SELECT * FROM plantillas_factura ORDER BY vecesUsada DESC")
    fun obtenerTodas(): Flow<List<PlantillaFactura>>

    @Query("SELECT * FROM plantillas_factura WHERE id = :id")
    suspend fun obtenerPorId(id: Long): PlantillaFactura?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(plantilla: PlantillaFactura): Long

    @Update
    suspend fun actualizar(plantilla: PlantillaFactura)

    @Delete
    suspend fun eliminar(plantilla: PlantillaFactura)
}

// DAO para FacturaRecurrente
@Dao
interface FacturaRecurrenteDao {
    @Query("SELECT * FROM facturas_recurrentes ORDER BY proximaFecha ASC")
    fun obtenerTodas(): Flow<List<FacturaRecurrente>>

    @Query("SELECT * FROM facturas_recurrentes WHERE activo = 1 AND proximaFecha <= :fecha")
    suspend fun obtenerPendientes(fecha: Date): List<FacturaRecurrente>

    @Query("SELECT * FROM facturas_recurrentes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): FacturaRecurrente?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recurrente: FacturaRecurrente): Long

    @Update
    suspend fun actualizar(recurrente: FacturaRecurrente)

    @Delete
    suspend fun eliminar(recurrente: FacturaRecurrente)
}

// DAO para PerfilImportacion
@Dao
interface PerfilImportacionDao {
    @Query("SELECT * FROM perfiles_importacion ORDER BY ultimoUso DESC")
    fun obtenerTodos(): Flow<List<PerfilImportacion>>

    @Query("SELECT * FROM perfiles_importacion WHERE id = :id")
    suspend fun obtenerPorId(id: Long): PerfilImportacion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(perfil: PerfilImportacion): Long

    @Update
    suspend fun actualizar(perfil: PerfilImportacion)

    @Delete
    suspend fun eliminar(perfil: PerfilImportacion)
}
