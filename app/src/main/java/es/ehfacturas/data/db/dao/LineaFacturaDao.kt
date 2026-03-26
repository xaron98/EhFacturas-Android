package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.LineaFactura
import kotlinx.coroutines.flow.Flow

@Dao
interface LineaFacturaDao {
    @Query("SELECT * FROM lineas_factura WHERE facturaId = :facturaId ORDER BY orden ASC")
    fun obtenerPorFactura(facturaId: Long): Flow<List<LineaFactura>>

    @Query("SELECT * FROM lineas_factura WHERE facturaId = :facturaId ORDER BY orden ASC")
    suspend fun obtenerPorFacturaSync(facturaId: Long): List<LineaFactura>

    @Query("SELECT * FROM lineas_factura WHERE id = :id")
    suspend fun obtenerPorId(id: Long): LineaFactura?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(linea: LineaFactura): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lineas: List<LineaFactura>): List<Long>

    @Update
    suspend fun actualizar(linea: LineaFactura)

    @Delete
    suspend fun eliminar(linea: LineaFactura)

    @Query("DELETE FROM lineas_factura WHERE facturaId = :facturaId")
    suspend fun eliminarPorFactura(facturaId: Long)
}
