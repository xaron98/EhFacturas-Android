package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lineas_factura",
    foreignKeys = [
        ForeignKey(
            entity = Factura::class,
            parentColumns = ["id"],
            childColumns = ["facturaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Articulo::class,
            parentColumns = ["id"],
            childColumns = ["articuloId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("facturaId"), Index("articuloId")]
)
data class LineaFactura(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orden: Int = 0,
    val facturaId: Long = 0,
    val articuloId: Long? = null,
    val referencia: String = "",
    val concepto: String = "",
    val cantidad: Double = 1.0,
    val unidad: UnidadMedida = UnidadMedida.UNIDAD,
    val precioUnitario: Double = 0.0,
    val descuentoPorcentaje: Double = 0.0,
    val porcentajeIVA: Double = 21.0,
    val subtotal: Double = 0.0
) {
    fun calcularSubtotal(): Double {
        val importeBruto = cantidad * precioUnitario
        val descuento = importeBruto * (descuentoPorcentaje / 100.0)
        return importeBruto - descuento
    }
}
