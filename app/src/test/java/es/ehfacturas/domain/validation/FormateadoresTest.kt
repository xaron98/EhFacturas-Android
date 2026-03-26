package es.ehfacturas.domain.validation

import org.junit.Assert.*
import org.junit.Test

class FormateadoresTest {
    @Test fun `parsear precio con punto decimal`() {
        assertEquals(123.45, Formateadores.parsearPrecio("123.45")!!, 0.001)
    }
    @Test fun `parsear precio con coma decimal espanola`() {
        assertEquals(123.45, Formateadores.parsearPrecio("123,45")!!, 0.001)
    }
    @Test fun `parsear precio con simbolo euro`() {
        assertEquals(123.45, Formateadores.parsearPrecio("123,45€")!!, 0.001)
        assertEquals(123.45, Formateadores.parsearPrecio("123,45 €")!!, 0.001)
    }
    @Test fun `parsear precio con espacios`() {
        assertEquals(50.0, Formateadores.parsearPrecio("  50  ")!!, 0.001)
    }
    @Test fun `parsear precio cero`() {
        assertEquals(0.0, Formateadores.parsearPrecio("0")!!, 0.001)
    }
    @Test fun `parsear precio invalido devuelve null`() {
        assertNull(Formateadores.parsearPrecio("abc"))
        assertNull(Formateadores.parsearPrecio(""))
        assertNull(Formateadores.parsearPrecio("12,34,56"))
    }
    @Test fun `formatear porcentaje`() {
        assertEquals("21,00%", Formateadores.formatearPorcentaje(21.0))
        assertEquals("4,00%", Formateadores.formatearPorcentaje(4.0))
    }
}
