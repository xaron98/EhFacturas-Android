package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.Gasto
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<Gasto>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Gasto?

    @Query("SELECT * FROM gastos WHERE categoria = :categoria ORDER BY fecha DESC")
    fun obtenerPorCategoria(categoria: String): Flow<List<Gasto>>

    @Query("SELECT * FROM gastos WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun obtenerPorRangoFechas(desde: Date, hasta: Date): Flow<List<Gasto>>

    @Query("SELECT COALESCE(SUM(importe), 0) FROM gastos WHERE fecha BETWEEN :desde AND :hasta")
    fun totalPeriodo(desde: Date, hasta: Date): Flow<Double>

    @Query("SELECT COUNT(*) FROM gastos")
    fun contarTodos(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: Gasto): Long

    @Update
    suspend fun actualizar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)
}
