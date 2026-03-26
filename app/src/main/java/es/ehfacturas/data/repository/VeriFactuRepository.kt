package es.ehfacturas.data.repository

import es.ehfacturas.data.db.dao.EventoSIFDao
import es.ehfacturas.data.db.dao.RegistroFacturacionDao
import es.ehfacturas.data.db.entity.EventoSIF
import es.ehfacturas.data.db.entity.RegistroFacturacion
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VeriFactuRepository @Inject constructor(
    private val registroDao: RegistroFacturacionDao,
    private val eventoDao: EventoSIFDao
) {
    // Registros
    fun obtenerRegistros(): Flow<List<RegistroFacturacion>> = registroDao.obtenerTodos()

    fun obtenerRegistrosPorFactura(facturaId: Long): Flow<List<RegistroFacturacion>> = registroDao.obtenerPorFactura(facturaId)

    suspend fun obtenerRegistroPorId(id: Long): RegistroFacturacion? = registroDao.obtenerPorId(id)

    suspend fun obtenerUltimoRegistro(): RegistroFacturacion? = registroDao.obtenerUltimo()

    suspend fun guardarRegistro(registro: RegistroFacturacion): Long = registroDao.insertar(registro)

    suspend fun actualizarRegistro(registro: RegistroFacturacion) = registroDao.actualizar(registro)

    // Eventos SIF
    fun obtenerEventos(): Flow<List<EventoSIF>> = eventoDao.obtenerTodos()

    fun obtenerEventosPorFactura(numeroFactura: String): Flow<List<EventoSIF>> = eventoDao.obtenerPorFactura(numeroFactura)

    suspend fun guardarEvento(evento: EventoSIF): Long = eventoDao.insertar(evento)

    suspend fun limpiarEventosAntiguos(fecha: Date) = eventoDao.limpiarAntiguos(fecha)
}
