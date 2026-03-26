// app/src/main/java/es/ehfacturas/ui/factura/components/TotalesSection.kt
package es.ehfacturas.ui.factura.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.ehfacturas.domain.validation.Formateadores
import es.ehfacturas.domain.validation.TotalesFactura

@Composable
fun TotalesSection(
    totales: TotalesFactura,
    descuentoGlobalPorcentaje: Double,
    aplicarIRPF: Boolean,
    irpfPorcentaje: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Totales",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            TotalRow("Base imponible", totales.baseImponible)

            if (descuentoGlobalPorcentaje > 0) {
                TotalRow(
                    "Descuento (${Formateadores.formatearDecimal(descuentoGlobalPorcentaje)}%)",
                    -(totales.baseImponible - totales.baseConDescuento)
                )
            }

            // Desglose IVA por porcentaje
            totales.desgloseIVA.toSortedMap(compareByDescending { it }).forEach { (porcentaje, importe) ->
                TotalRow("IVA ${Formateadores.formatearDecimal(porcentaje)}%", importe)
            }

            if (aplicarIRPF && totales.totalIRPF > 0) {
                TotalRow("IRPF ${Formateadores.formatearDecimal(irpfPorcentaje)}%", -totales.totalIRPF)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Formateadores.formatearMoneda(totales.totalFactura),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TotalRow(etiqueta: String, valor: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = Formateadores.formatearMoneda(valor),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
