package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val nif: String = "",
    val direccion: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = "",
    val telefono: String = "",
    val email: String = "",
    val observaciones: String = "",
    val fechaCreacion: Date = Date(),
    val fechaModificacion: Date = Date(),
    val activo: Boolean = true
) {
    val iniciales: String
        get() {
            val partes = nombre.trim().split("\\s+".toRegex())
            return when {
                partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
                partes.isNotEmpty() && partes[0].isNotEmpty() -> partes[0].first().uppercaseChar().toString()
                else -> "?"
            }
        }
}
