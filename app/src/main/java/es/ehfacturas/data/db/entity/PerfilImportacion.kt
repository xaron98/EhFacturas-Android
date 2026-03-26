package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "perfiles_importacion")
data class PerfilImportacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val tipo: String = "",
    val separador: String = ";",
    val encoding: String = "utf8",
    val mapeoJSON: String = "{}",
    val cabecerasOriginales: List<String> = emptyList(),
    val fechaCreacion: Date = Date(),
    val ultimoUso: Date = Date(),
    val vecesUsado: Int = 0
)
