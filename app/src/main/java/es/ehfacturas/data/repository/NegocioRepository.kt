package es.ehfacturas.data.repository

import es.ehfacturas.data.db.dao.NegocioDao
import es.ehfacturas.data.db.entity.Negocio
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NegocioRepository @Inject constructor(
    private val negocioDao: NegocioDao
) {
    fun obtenerNegocio(): Flow<Negocio?> = negocioDao.obtenerNegocio()

    suspend fun obtenerNegocioSync(): Negocio? = negocioDao.obtenerNegocioSync()

    suspend fun guardar(negocio: Negocio): Long = negocioDao.insertar(negocio)

    suspend fun actualizar(negocio: Negocio) = negocioDao.actualizar(negocio)

    suspend fun incrementarNumeroFactura(id: Long) = negocioDao.incrementarNumeroFactura(id)
}
