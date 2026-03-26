package es.ehfacturas.data.db.dao

import androidx.room.*
import es.ehfacturas.data.db.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY orden ASC")
    fun obtenerTodas(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Categoria?

    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(categoria: Categoria): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(categorias: List<Categoria>): List<Long>

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)
}
