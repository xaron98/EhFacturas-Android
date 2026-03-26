package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "eventos_sif")
data class EventoSIF(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Date = Date(),
    val tipo: String = "",
    val descripcion: String = "",
    val detalles: String = "",
    val numeroFactura: String = "",
    val usuario: String = ""
)
