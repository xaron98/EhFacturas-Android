package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.RegistroFacturacion
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroFacturacionDao {
    @Query("SELECT * FROM registros_facturacion ORDER BY fechaHoraGeneracion DESC")
    fun obtenerTodos(): Flow<List<RegistroFacturacion>>

    @Query("SELECT * FROM registros_facturacion WHERE facturaId = :facturaId ORDER BY fechaHoraGeneracion DESC")
    fun obtenerPorFactura(facturaId: Long): Flow<List<RegistroFacturacion>>

    @Query("SELECT * FROM registros_facturacion WHERE id = :id")
    suspend fun obtenerPorId(id: Long): RegistroFacturacion?

    @Query("SELECT * FROM registros_facturacion ORDER BY fechaHoraGeneracion DESC LIMIT 1")
    suspend fun obtenerUltimo(): RegistroFacturacion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(registro: RegistroFacturacion): Long

    @Query("SELECT * FROM registros_facturacion ORDER BY fechaHoraGeneracion ASC")
    suspend fun obtenerTodosSync(): List<RegistroFacturacion>

    @Update
    suspend fun actualizar(registro: RegistroFacturacion)
}
