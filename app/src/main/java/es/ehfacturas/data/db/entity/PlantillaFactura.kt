package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "plantillas_factura")
data class PlantillaFactura(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val articulosTexto: String = "",
    val observaciones: String = "",
    val fechaCreacion: Date = Date(),
    val vecesUsada: Int = 0
)
