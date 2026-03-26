package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concepto: String = "",
    val importe: Double = 0.0,
    val fecha: Date = Date(),
    val categoria: String = "",
    val proveedor: String = "",
    val deducibleIVA: Boolean = true,
    val importeIVA: Double = 0.0,
    val observaciones: String = "",
    val fechaCreacion: Date = Date()
) {
    companion object {
        val categoriasGasto = listOf(
            "material", "herramientas", "vehiculo", "oficina", "otros"
        )
    }
}
