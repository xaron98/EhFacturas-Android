package es.ehfacturas.domain.validation

import es.ehfacturas.data.db.entity.LineaFactura
import org.junit.Assert.*
import org.junit.Test

class RecalculoTotalesTest {
    @Test fun `recalcular linea simple sin descuento`() {
        val linea = LineaFactura(concepto = "Consultoría", cantidad = 3.0, precioUnitario = 50.0, porcentajeIVA = 21.0)
        assertEquals(150.0, linea.calcularSubtotal(), 0.001)
    }
    @Test fun `recalcular linea con descuento`() {
        val linea = LineaFactura(concepto = "Producto", cantidad = 2.0, precioUnitario = 100.0, descuentoPorcentaje = 10.0, porcentajeIVA = 21.0)
        assertEquals(180.0, linea.calcularSubtotal(), 0.001)
    }
    @Test fun `recalcular totales factura con multiples lineas e IVA mixto`() {
        val lineas = listOf(
            LineaFactura(concepto = "Servicio", cantidad = 1.0, precioUnitario = 1000.0, porcentajeIVA = 21.0),
            LineaFactura(concepto = "Libro", cantidad = 2.0, precioUnitario = 20.0, porcentajeIVA = 4.0)
        )
        val totales = RecalculoTotales.calcular(lineas, 0.0, true, 15.0)
        assertEquals(1040.0, totales.baseImponible, 0.001)
        assertEquals(211.6, totales.totalIVA, 0.001)
        assertEquals(156.0, totales.totalIRPF, 0.001)
        assertEquals(1095.6, totales.totalFactura, 0.001)
    }
    @Test fun `recalcular con descuento global`() {
        val lineas = listOf(LineaFactura(concepto = "Servicio", cantidad = 1.0, precioUnitario = 1000.0, porcentajeIVA = 21.0))
        val totales = RecalculoTotales.calcular(lineas, 10.0, false, 0.0)
        assertEquals(1000.0, totales.baseImponible, 0.001)
        assertEquals(900.0, totales.baseConDescuento, 0.001)
        assertEquals(189.0, totales.totalIVA, 0.001)
        assertEquals(1089.0, totales.totalFactura, 0.001)
    }
    @Test fun `desglose IVA agrupa por porcentaje`() {
        val lineas = listOf(
            LineaFactura(concepto = "A", cantidad = 1.0, precioUnitario = 100.0, porcentajeIVA = 21.0),
            LineaFactura(concepto = "B", cantidad = 1.0, precioUnitario = 200.0, porcentajeIVA = 21.0),
            LineaFactura(concepto = "C", cantidad = 1.0, precioUnitario = 50.0, porcentajeIVA = 10.0)
        )
        val totales = RecalculoTotales.calcular(lineas, 0.0, false, 0.0)
        assertEquals(2, totales.desgloseIVA.size)
        assertEquals(63.0, totales.desgloseIVA[21.0]!!, 0.001)
        assertEquals(5.0, totales.desgloseIVA[10.0]!!, 0.001)
    }
    @Test fun `sin lineas devuelve todo cero`() {
        val totales = RecalculoTotales.calcular(emptyList(), 0.0, false, 0.0)
        assertEquals(0.0, totales.baseImponible, 0.001)
        assertEquals(0.0, totales.totalFactura, 0.001)
    }
}
