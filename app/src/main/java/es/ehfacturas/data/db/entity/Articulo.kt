package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "articulos",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoriaId")]
)
data class Articulo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referencia: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precioUnitario: Double = 0.0,
    val precioCoste: Double = 0.0,
    val unidad: UnidadMedida = UnidadMedida.UNIDAD,
    val tipoIVA: TipoIVA = TipoIVA.GENERAL,
    val proveedor: String = "",
    val urlProveedor: String = "",
    val referenciaProveedor: String = "",
    val categoriaId: Long? = null,
    val etiquetas: List<String> = emptyList(),
    val activo: Boolean = true,
    val fechaCreacion: Date = Date(),
    val fechaModificacion: Date = Date()
) {
    val margen: Double
        get() = if (precioCoste > 0) ((precioUnitario - precioCoste) / precioCoste) * 100.0 else 0.0

    val precioConIVA: Double
        get() = precioUnitario * (1 + tipoIVA.porcentaje / 100.0)
}
