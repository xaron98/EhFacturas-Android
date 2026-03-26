package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.Negocio
import kotlinx.coroutines.flow.Flow

@Dao
interface NegocioDao {
    @Query("SELECT * FROM negocios LIMIT 1")
    fun obtenerNegocio(): Flow<Negocio?>

    @Query("SELECT * FROM negocios LIMIT 1")
    suspend fun obtenerNegocioSync(): Negocio?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(negocio: Negocio): Long

    @Update
    suspend fun actualizar(negocio: Negocio)

    @Query("UPDATE negocios SET siguienteNumero = siguienteNumero + 1 WHERE id = :id")
    suspend fun incrementarNumeroFactura(id: Long)
}
