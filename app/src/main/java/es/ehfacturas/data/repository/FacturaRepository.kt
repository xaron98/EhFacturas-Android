package es.ehfacturas.data.repository

import es.ehfacturas.data.db.dao.ClienteTotal
import es.ehfacturas.data.db.dao.FacturaDao
import es.ehfacturas.data.db.dao.LineaFacturaDao
import es.ehfacturas.data.db.entity.EstadoFactura
import es.ehfacturas.data.db.entity.Factura
import es.ehfacturas.data.db.entity.LineaFactura
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacturaRepository @Inject constructor(
    private val facturaDao: FacturaDao,
    private val lineaFacturaDao: LineaFacturaDao
) {
    fun obtenerTodas(): Flow<List<Factura>> = facturaDao.obtenerTodas()

    suspend fun obtenerPorId(id: Long): Factura? = facturaDao.obtenerPorId(id)

    fun obtenerPorEstado(estado: EstadoFactura): Flow<List<Factura>> = facturaDao.obtenerPorEstado(estado)

    fun obtenerPorCliente(clienteId: Long): Flow<List<Factura>> = facturaDao.obtenerPorCliente(clienteId)

    fun obtenerPorRangoFechas(desde: Date, hasta: Date): Flow<List<Factura>> = facturaDao.obtenerPorRangoFechas(desde, hasta)

    suspend fun buscar(texto: String): List<Factura> = facturaDao.buscar(texto)

    fun contarTodas(): Flow<Int> = facturaDao.contarTodas()

    fun contarPorEstado(estado: EstadoFactura): Flow<Int> = facturaDao.contarPorEstado(estado)

    fun facturacionPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.facturacionPeriodo(desde, hasta)

    suspend fun obtenerVencidas(fecha: Date): List<Factura> = facturaDao.obtenerVencidas(fecha)

    fun cobradoPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.cobradoPeriodo(desde, hasta)

    fun ivaPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.ivaPeriodo(desde, hasta)

    fun irpfPeriodo(desde: Date, hasta: Date): Flow<Double> = facturaDao.irpfPeriodo(desde, hasta)

    fun contarEmitidasPeriodo(desde: Date, hasta: Date): Flow<Int> = facturaDao.contarEmitidasPeriodo(desde, hasta)

    suspend fun topClientes(desde: Date, hasta: Date, limit: Int): List<ClienteTotal> = facturaDao.topClientes(desde, hasta, limit)

    suspend fun facturasParaExportar(desde: Date, hasta: Date): List<Factura> = facturaDao.facturasParaExportar(desde, hasta)

    suspend fun guardar(factura: Factura): Long = facturaDao.insertar(factura)

    suspend fun actualizar(factura: Factura) = facturaDao.actualizar(factura)

    suspend fun eliminar(factura: Factura) = facturaDao.eliminar(factura)

    // Líneas de factura
    fun obtenerLineas(facturaId: Long): Flow<List<LineaFactura>> = lineaFacturaDao.obtenerPorFactura(facturaId)

    suspend fun obtenerLineasSync(facturaId: Long): List<LineaFactura> = lineaFacturaDao.obtenerPorFacturaSync(facturaId)

    suspend fun guardarLinea(linea: LineaFactura): Long = lineaFacturaDao.insertar(linea)

    suspend fun guardarLineas(lineas: List<LineaFactura>): List<Long> = lineaFacturaDao.insertarTodas(lineas)

    suspend fun actualizarLinea(linea: LineaFactura) = lineaFacturaDao.actualizar(linea)

    suspend fun eliminarLinea(linea: LineaFactura) = lineaFacturaDao.eliminar(linea)

    suspend fun eliminarLineasDeFactura(facturaId: Long) = lineaFacturaDao.eliminarPorFactura(facturaId)
}
