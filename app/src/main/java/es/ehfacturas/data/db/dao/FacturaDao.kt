package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.EstadoFactura
import es.ehfacturas.data.db.entity.Factura
import es.ehfacturas.data.db.entity.LineaFactura
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FacturaDao {
    @Query("SELECT * FROM facturas ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Factura>>

    @Query("SELECT * FROM facturas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Factura?

    @Query("SELECT * FROM facturas WHERE estado = :estado ORDER BY fecha DESC")
    fun obtenerPorEstado(estado: EstadoFactura): Flow<List<Factura>>

    @Query("SELECT * FROM facturas WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun obtenerPorCliente(clienteId: Long): Flow<List<Factura>>

    @Query("SELECT * FROM facturas WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun obtenerPorRangoFechas(desde: Date, hasta: Date): Flow<List<Factura>>

    @Query("SELECT * FROM facturas WHERE numeroFactura LIKE '%' || :texto || '%' OR clienteNombre LIKE '%' || :texto || '%'")
    suspend fun buscar(texto: String): List<Factura>

    @Query("SELECT COUNT(*) FROM facturas")
    fun contarTodas(): Flow<Int>

    @Query("SELECT COUNT(*) FROM facturas WHERE estado = :estado")
    fun contarPorEstado(estado: EstadoFactura): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalFactura), 0) FROM facturas WHERE estado IN ('EMITIDA', 'PAGADA') AND fecha BETWEEN :desde AND :hasta")
    fun facturacionPeriodo(desde: Date, hasta: Date): Flow<Double>

    @Query("SELECT * FROM facturas WHERE estado = 'EMITIDA' AND fechaVencimiento IS NOT NULL AND fechaVencimiento < :fecha")
    suspend fun obtenerVencidas(fecha: Date): List<Factura>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(factura: Factura): Long

    @Update
    suspend fun actualizar(factura: Factura)

    @Delete
    suspend fun eliminar(factura: Factura)
}
