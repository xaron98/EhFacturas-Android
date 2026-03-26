package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.Articulo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticuloDao {
    @Query("SELECT * FROM articulos WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Articulo>>

    @Query("SELECT * FROM articulos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Articulo?

    @Query("SELECT * FROM articulos WHERE categoriaId = :categoriaId AND activo = 1 ORDER BY nombre ASC")
    fun obtenerPorCategoria(categoriaId: Long): Flow<List<Articulo>>

    @Query("SELECT * FROM articulos WHERE activo = 1 AND (nombre LIKE '%' || :texto || '%' OR referencia LIKE '%' || :texto || '%' OR descripcion LIKE '%' || :texto || '%')")
    suspend fun buscar(texto: String): List<Articulo>

    @Query("SELECT COUNT(*) FROM articulos WHERE activo = 1")
    fun contarActivos(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(articulo: Articulo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(articulos: List<Articulo>): List<Long>

    @Update
    suspend fun actualizar(articulo: Articulo)

    @Delete
    suspend fun eliminar(articulo: Articulo)
}
