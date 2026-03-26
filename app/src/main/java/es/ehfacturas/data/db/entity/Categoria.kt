package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val icono: String = "folder",
    val orden: Int = 0
) {
    companion object {
        val categoriasDefecto = listOf(
            "General" to "folder",
            "Servicios" to "build",
            "Materiales" to "inventory_2",
            "Consultoría" to "school",
            "Diseño" to "palette",
            "Desarrollo" to "code",
            "Formación" to "menu_book",
            "Transporte" to "local_shipping",
            "Alquiler" to "home",
            "Otros" to "more_horiz"
        )
    }
}
