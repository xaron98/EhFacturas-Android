package es.ehfacturas.data.repository

import es.ehfacturas.data.db.dao.ClienteDao
import es.ehfacturas.data.db.entity.Cliente
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRepository @Inject constructor(
    private val clienteDao: ClienteDao
) {
    fun obtenerTodos(): Flow<List<Cliente>> = clienteDao.obtenerTodos()

    fun obtenerTodosIncluidosInactivos(): Flow<List<Cliente>> = clienteDao.obtenerTodosIncluidosInactivos()

    suspend fun obtenerPorId(id: Long): Cliente? = clienteDao.obtenerPorId(id)

    suspend fun buscar(texto: String): List<Cliente> = clienteDao.buscar(texto)

    fun contarActivos(): Flow<Int> = clienteDao.contarActivos()

    suspend fun guardar(cliente: Cliente): Long = clienteDao.insertar(cliente)

    suspend fun actualizar(cliente: Cliente) = clienteDao.actualizar(cliente)

    suspend fun eliminar(cliente: Cliente) = clienteDao.eliminar(cliente)
}
