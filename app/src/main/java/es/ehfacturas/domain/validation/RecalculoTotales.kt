package es.ehfacturas.domain.validation

import es.ehfacturas.data.db.entity.LineaFactura

data class TotalesFactura(
    val baseImponible: Double,
    val baseConDescuento: Double,
    val totalIVA: Double,
    val totalIRPF: Double,
    val totalFactura: Double,
    val desgloseIVA: Map<Double, Double>
)

object RecalculoTotales {
    fun calcular(
        lineas: List<LineaFactura>,
        descuentoGlobalPorcentaje: Double,
        aplicarIRPF: Boolean,
        irpfPorcentaje: Double
    ): TotalesFactura {
        val lineasConSubtotal = lineas.map { it.copy(subtotal = it.calcularSubtotal()) }
        val baseImponible = lineasConSubtotal.sumOf { it.subtotal }
        val descuentoGlobal = baseImponible * descuentoGlobalPorcentaje / 100.0
        val baseConDescuento = baseImponible - descuentoGlobal
        val factorDescuento = if (baseImponible > 0) baseConDescuento / baseImponible else 0.0

        val desgloseIVA = lineasConSubtotal
            .groupBy { it.porcentajeIVA }
            .mapValues { (porcentaje, lineasGrupo) ->
                val baseGrupo = lineasGrupo.sumOf { it.subtotal }
                baseGrupo * factorDescuento * porcentaje / 100.0
            }
            .filter { it.value > 0.0 }

        val totalIVA = desgloseIVA.values.sum()
        val totalIRPF = if (aplicarIRPF) baseConDescuento * irpfPorcentaje / 100.0 else 0.0
        val totalFactura = baseConDescuento + totalIVA - totalIRPF

        return TotalesFactura(baseImponible, baseConDescuento, totalIVA, totalIRPF, totalFactura, desgloseIVA)
    }
}
