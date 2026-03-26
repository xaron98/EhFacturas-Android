package es.ehfacturas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "negocios")
data class Negocio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val nif: String = "",
    val direccion: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = "",
    val telefono: String = "",
    val email: String = "",
    val logoRuta: String? = null,
    val ivaGeneral: Double = 21.0,
    val ivaReducido: Double = 10.0,
    val irpfPorcentaje: Double = 15.0,
    val aplicarIRPF: Boolean = false,
    val prefijoFactura: String = "FAC-",
    val siguienteNumero: Int = 1,
    val notas: String = "Pago a 30 días.",
    val usarEntornoPruebas: Boolean = true,
    val certificadoInstalado: Boolean = false,
    val certificadoCaducidad: Date? = null,
    val envioAutomatico: Boolean = false,
    val cloudProvider: String = "claude",
    val temaApp: String = "auto"
) {
    fun generarNumeroFactura(): String {
        val numero = siguienteNumero.toString().padStart(4, '0')
        return "$prefijoFactura$numero"
    }
}
