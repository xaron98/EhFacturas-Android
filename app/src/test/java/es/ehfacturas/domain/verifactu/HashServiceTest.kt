// app/src/test/java/es/ehfacturas/domain/verifactu/HashServiceTest.kt
package es.ehfacturas.domain.verifactu

import es.ehfacturas.data.db.entity.*
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class HashServiceTest {

    private val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES"))

    @Test
    fun `calcular hash produce hex SHA-256 de 64 caracteres`() {
        val registro = RegistroFacturacion(
            nifEmisor = "B12345678",
            numeroFactura = "FAC-0001",
            serieFactura = "FAC-",
            fechaExpedicion = formatoFecha.parse("26-03-2026")!!,
            tipoFactura = TipoFacturaVF.COMPLETA,
            importeTotal = 1210.0,
            hashRegistroAnterior = "",
            fechaHoraGeneracion = formatoFecha.parse("26-03-2026")!!
        )
        val hash = HashService.calcularHash(registro)
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun `hash diferente para datos diferentes`() {
        val base = RegistroFacturacion(
            nifEmisor = "B12345678",
            numeroFactura = "FAC-0001",
            serieFactura = "FAC-",
            fechaExpedicion = formatoFecha.parse("26-03-2026")!!,
            tipoFactura = TipoFacturaVF.COMPLETA,
            importeTotal = 1210.0,
            hashRegistroAnterior = "",
            fechaHoraGeneracion = formatoFecha.parse("26-03-2026")!!
        )
        val modificado = base.copy(importeTotal = 1211.0)
        assertNotEquals(HashService.calcularHash(base), HashService.calcularHash(modificado))
    }

    @Test
    fun `hash encadenado cambia con hash anterior diferente`() {
        val registro = RegistroFacturacion(
            nifEmisor = "B12345678",
            numeroFactura = "FAC-0002",
            serieFactura = "FAC-",
            fechaExpedicion = formatoFecha.parse("27-03-2026")!!,
            tipoFactura = TipoFacturaVF.COMPLETA,
            importeTotal = 500.0,
            hashRegistroAnterior = "",
            fechaHoraGeneracion = formatoFecha.parse("27-03-2026")!!
        )
        val conAnterior = registro.copy(hashRegistroAnterior = "abc123")
        assertNotEquals(
            HashService.calcularHash(registro),
            HashService.calcularHash(conAnterior)
        )
    }

    @Test
    fun `hash deterministico mismo input mismo output`() {
        val registro = RegistroFacturacion(
            nifEmisor = "B12345678",
            numeroFactura = "FAC-0001",
            serieFactura = "FAC-",
            fechaExpedicion = formatoFecha.parse("26-03-2026")!!,
            tipoFactura = TipoFacturaVF.COMPLETA,
            importeTotal = 1210.0,
            hashRegistroAnterior = "",
            fechaHoraGeneracion = formatoFecha.parse("26-03-2026")!!
        )
        assertEquals(HashService.calcularHash(registro), HashService.calcularHash(registro))
    }

    @Test
    fun `extraer serie de numero factura`() {
        assertEquals("FAC-", HashService.extraerSerie("FAC-0001"))
        assertEquals("F-2026-", HashService.extraerSerie("F-2026-042"))
        assertEquals("", HashService.extraerSerie("0001"))
    }
}
