package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun obtenerTodosIncluidosInactivos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Cliente?

    @Query("SELECT * FROM clientes WHERE nombre LIKE '%' || :texto || '%' OR nif LIKE '%' || :texto || '%'")
    suspend fun buscar(texto: String): List<Cliente>

    @Query("SELECT COUNT(*) FROM clientes WHERE activo = 1")
    fun contarActivos(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: Cliente): Long

    @Update
    suspend fun actualizar(cliente: Cliente)

    @Delete
    suspend fun eliminar(cliente: Cliente)
}
