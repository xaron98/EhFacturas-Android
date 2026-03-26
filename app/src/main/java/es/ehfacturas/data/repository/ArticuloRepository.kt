package es.ehfacturas.data.repository

import es.ehfacturas.data.db.dao.ArticuloDao
import es.ehfacturas.data.db.dao.CategoriaDao
import es.ehfacturas.data.db.entity.Articulo
import es.ehfacturas.data.db.entity.Categoria
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticuloRepository @Inject constructor(
    private val articuloDao: ArticuloDao,
    private val categoriaDao: CategoriaDao
) {
    fun obtenerTodos(): Flow<List<Articulo>> = articuloDao.obtenerTodos()

    suspend fun obtenerPorId(id: Long): Articulo? = articuloDao.obtenerPorId(id)

    fun obtenerPorCategoria(categoriaId: Long): Flow<List<Articulo>> = articuloDao.obtenerPorCategoria(categoriaId)

    suspend fun buscar(texto: String): List<Articulo> = articuloDao.buscar(texto)

    suspend fun obtenerTodosSync(): List<Articulo> = articuloDao.obtenerTodosSync()

    fun contarActivos(): Flow<Int> = articuloDao.contarActivos()

    suspend fun guardar(articulo: Articulo): Long = articuloDao.insertar(articulo)

    suspend fun guardarTodos(articulos: List<Articulo>): List<Long> = articuloDao.insertarTodos(articulos)

    suspend fun actualizar(articulo: Articulo) = articuloDao.actualizar(articulo)

    suspend fun eliminar(articulo: Articulo) = articuloDao.eliminar(articulo)

    // Categorías
    fun obtenerCategorias(): Flow<List<Categoria>> = categoriaDao.obtenerTodas()

    suspend fun obtenerCategoriaPorId(id: Long): Categoria? = categoriaDao.obtenerPorId(id)

    suspend fun guardarCategoria(categoria: Categoria): Long = categoriaDao.insertar(categoria)
}
