package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.EventoSIF
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoSIFDao {
    @Query("SELECT * FROM eventos_sif ORDER BY timestamp DESC")
    fun obtenerTodos(): Flow<List<EventoSIF>>

    @Query("SELECT * FROM eventos_sif WHERE numeroFactura = :numeroFactura ORDER BY timestamp DESC")
    fun obtenerPorFactura(numeroFactura: String): Flow<List<EventoSIF>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evento: EventoSIF): Long

    @Query("DELETE FROM eventos_sif WHERE timestamp < :fecha")
    suspend fun limpiarAntiguos(fecha: java.util.Date)
}
