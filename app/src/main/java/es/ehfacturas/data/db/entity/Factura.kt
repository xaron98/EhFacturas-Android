package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "facturas",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Factura::class,
            parentColumns = ["id"],
            childColumns = ["facturaRectificadaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("clienteId"), Index("facturaRectificadaId")]
)
data class Factura(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroFactura: String = "",
    val fecha: Date = Date(),
    val fechaVencimiento: Date? = null,
    val estado: EstadoFactura = EstadoFactura.BORRADOR,
    val clienteId: Long? = null,
    val clienteNombre: String = "",
    val clienteNIF: String = "",
    val clienteDireccion: String = "",
    val baseImponible: Double = 0.0,
    val totalIVA: Double = 0.0,
    val totalIRPF: Double = 0.0,
    val totalFactura: Double = 0.0,
    val descuentoGlobalPorcentaje: Double = 0.0,
    val observaciones: String = "",
    val notasInternas: String = "",
    val promptOriginal: String? = null,
    val pdfRuta: String? = null,
    val firmaClienteRuta: String? = null,
    val fechaCreacion: Date = Date(),
    val fechaModificacion: Date = Date(),
    val tipoFactura: TipoFacturaVF = TipoFacturaVF.COMPLETA,
    val facturaRectificadaId: Long? = null
)
