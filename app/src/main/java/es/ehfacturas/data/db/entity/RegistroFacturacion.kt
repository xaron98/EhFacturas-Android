package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "registros_facturacion",
    foreignKeys = [
        ForeignKey(
            entity = Factura::class,
            parentColumns = ["id"],
            childColumns = ["facturaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("facturaId")]
)
data class RegistroFacturacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipoRegistro: TipoRegistro = TipoRegistro.ALTA,
    val nifEmisor: String = "",
    val numeroFactura: String = "",
    val serieFactura: String = "",
    val fechaExpedicion: Date = Date(),
    val tipoFactura: TipoFacturaVF = TipoFacturaVF.COMPLETA,
    val facturaRectificadaNumero: String? = null,
    val descripcionOperacion: String = "",
    val baseImponible: Double = 0.0,
    val totalIVA: Double = 0.0,
    val totalIRPF: Double = 0.0,
    val importeTotal: Double = 0.0,
    val nifDestinatario: String = "",
    val nombreDestinatario: String = "",
    val hashRegistro: String = "",
    val hashRegistroAnterior: String = "",
    val fechaHoraGeneracion: Date = Date(),
    val facturaId: Long = 0,
    val estadoEnvio: EstadoEnvioVF = EstadoEnvioVF.NO_ENVIADO,
    val respuestaAEAT: String = "",
    val fechaEnvio: Date? = null
)
