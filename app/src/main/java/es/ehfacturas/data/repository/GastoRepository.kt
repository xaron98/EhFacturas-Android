package es.ehfacturas.data.repository

import es.ehfacturas.data.db.dao.GastoDao
import es.ehfacturas.data.db.entity.Gasto
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GastoRepository @Inject constructor(
    private val gastoDao: GastoDao
) {
    fun obtenerTodos(): Flow<List<Gasto>> = gastoDao.obtenerTodos()

    suspend fun obtenerPorId(id: Long): Gasto? = gastoDao.obtenerPorId(id)

    fun obtenerPorCategoria(categoria: String): Flow<List<Gasto>> = gastoDao.obtenerPorCategoria(categoria)

    fun obtenerPorRangoFechas(desde: Date, hasta: Date): Flow<List<Gasto>> = gastoDao.obtenerPorRangoFechas(desde, hasta)

    fun totalPeriodo(desde: Date, hasta: Date): Flow<Double> = gastoDao.totalPeriodo(desde, hasta)

    fun contarTodos(): Flow<Int> = gastoDao.contarTodos()

    suspend fun guardar(gasto: Gasto): Long = gastoDao.insertar(gasto)

    suspend fun actualizar(gasto: Gasto) = gastoDao.actualizar(gasto)

    suspend fun eliminar(gasto: Gasto) = gastoDao.eliminar(gasto)
}
