package es.ehfacturas.domain.validation

import java.text.NumberFormat
import java.util.Locale

object Formateadores {
    private val localeES = Locale("es", "ES")
    private val formatoMoneda = NumberFormat.getCurrencyInstance(localeES)

    fun parsearPrecio(texto: String): Double? {
        val limpio = texto.replace(",", ".").replace("€", "").trim()
        if (limpio.isEmpty()) return null
        if (limpio.count { it == '.' } > 1) return null
        return limpio.toDoubleOrNull()
    }

    fun formatearMoneda(valor: Double): String = formatoMoneda.format(valor)
    fun formatearPorcentaje(valor: Double): String = String.format(localeES, "%.2f%%", valor)
    fun formatearDecimal(valor: Double): String = String.format(localeES, "%.2f", valor)
}
