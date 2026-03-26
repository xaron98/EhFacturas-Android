package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.Calendar

@Entity(
    tableName = "facturas_recurrentes",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("clienteId")]
)
data class FacturaRecurrente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val clienteNombre: String = "",
    val clienteNIF: String = "",
    val articulosTexto: String = "",
    val importeTotal: Double = 0.0,
    val frecuencia: String = "mensual",
    val proximaFecha: Date = Date(),
    val activo: Boolean = true,
    val vecesGenerada: Int = 0,
    val fechaCreacion: Date = Date(),
    val clienteId: Long? = null
) {
    companion object {
        fun calcularProximaFecha(desde: Date, frecuencia: String): Date {
            val calendario = Calendar.getInstance()
            calendario.time = desde
            when (frecuencia) {
                "semanal" -> calendario.add(Calendar.WEEK_OF_YEAR, 1)
                "mensual" -> calendario.add(Calendar.MONTH, 1)
                "trimestral" -> calendario.add(Calendar.MONTH, 3)
                "anual" -> calendario.add(Calendar.YEAR, 1)
            }
            return calendario.time
        }
    }
}
