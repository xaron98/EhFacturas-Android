// app/src/main/java/es/ehfacturas/domain/verifactu/HashService.kt
package es.ehfacturas.domain.verifactu

import es.ehfacturas.data.db.dao.RegistroFacturacionDao
import es.ehfacturas.data.db.entity.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

data class CadenaVerificacion(
    val valida: Boolean,
    val totalRegistros: Int,
    val errores: List<String>
)

@Singleton
class HashService @Inject constructor(
    private val registroDao: RegistroFacturacionDao
) {
    companion object {
        private val formatoFechaHash = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES"))
        private val formatoFechaHoraHash = SimpleDateFormat("dd-MM-yyyy HH:mm:ssZ", Locale("es", "ES")).apply {
            timeZone = TimeZone.getTimeZone("Europe/Madrid")
        }

        fun calcularHash(registro: RegistroFacturacion): String {
            val tipoFacturaStr = when (registro.tipoFactura) {
                TipoFacturaVF.COMPLETA -> "completa"
                TipoFacturaVF.SIMPLIFICADA -> "simplificada"
                TipoFacturaVF.RECTIFICATIVA -> "rectificativa"
            }

            val campos = listOf(
                registro.nifEmisor,
                registro.numeroFactura,
                registro.serieFactura,
                formatoFechaHash.format(registro.fechaExpedicion),
                tipoFacturaStr,
                String.format(Locale.US, "%.2f", registro.importeTotal),
                registro.hashRegistroAnterior,
                formatoFechaHoraHash.format(registro.fechaHoraGeneracion)
            )

            val cadena = campos.joinToString("|")
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(cadena.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun extraerSerie(numeroFactura: String): String {
            val ultimoGuion = numeroFactura.lastIndexOf('-')
            return if (ultimoGuion >= 0) numeroFactura.substring(0, ultimoGuion + 1) else ""
        }
    }

    suspend fun obtenerHashAnterior(): String {
        return registroDao.obtenerUltimo()?.hashRegistro ?: ""
    }

    suspend fun crearRegistroAlta(factura: Factura, negocio: Negocio): RegistroFacturacion {
        val hashAnterior = obtenerHashAnterior()
        val ahora = Date()

        val registro = RegistroFacturacion(
            tipoRegistro = TipoRegistro.ALTA,
            nifEmisor = negocio.nif,
            numeroFactura = factura.numeroFactura,
            serieFactura = extraerSerie(factura.numeroFactura),
            fechaExpedicion = factura.fecha,
            tipoFactura = factura.tipoFactura,
            descripcionOperacion = "Emisión de factura",
            baseImponible = factura.baseImponible,
            totalIVA = factura.totalIVA,
            totalIRPF = factura.totalIRPF,
            importeTotal = factura.totalFactura,
            nifDestinatario = factura.clienteNIF,
            nombreDestinatario = factura.clienteNombre,
            hashRegistroAnterior = hashAnterior,
            fechaHoraGeneracion = ahora,
            facturaId = factura.id,
            estadoEnvio = EstadoEnvioVF.NO_ENVIADO
        )

        val hash = calcularHash(registro)
        val registroConHash = registro.copy(hashRegistro = hash)
        val id = registroDao.insertar(registroConHash)
        return registroConHash.copy(id = id)
    }

    suspend fun crearRegistroAnulacion(factura: Factura, negocio: Negocio): RegistroFacturacion {
        val hashAnterior = obtenerHashAnterior()
        val ahora = Date()

        val registro = RegistroFacturacion(
            tipoRegistro = TipoRegistro.ANULACION,
            nifEmisor = negocio.nif,
            numeroFactura = factura.numeroFactura,
            serieFactura = extraerSerie(factura.numeroFactura),
            fechaExpedicion = factura.fecha,
            tipoFactura = factura.tipoFactura,
            descripcionOperacion = "Anulación de factura",
            baseImponible = factura.baseImponible,
            totalIVA = factura.totalIVA,
            totalIRPF = factura.totalIRPF,
            importeTotal = factura.totalFactura,
            nifDestinatario = factura.clienteNIF,
            nombreDestinatario = factura.clienteNombre,
            hashRegistroAnterior = hashAnterior,
            fechaHoraGeneracion = ahora,
            facturaId = factura.id,
            estadoEnvio = EstadoEnvioVF.NO_ENVIADO
        )

        val hash = calcularHash(registro)
        val registroConHash = registro.copy(hashRegistro = hash)
        val id = registroDao.insertar(registroConHash)
        return registroConHash.copy(id = id)
    }

    suspend fun verificarCadena(): CadenaVerificacion {
        // Obtener todos los registros en orden cronológico
        val registros = registroDao.obtenerTodosSync()
        if (registros.isEmpty()) return CadenaVerificacion(true, 0, emptyList())

        val errores = mutableListOf<String>()
        var hashAnterior = ""

        registros.forEach { registro ->
            // Verificar que hashRegistroAnterior coincide con el hash del anterior
            if (registro.hashRegistroAnterior != hashAnterior) {
                errores.add("Registro ${registro.id}: hashRegistroAnterior no coincide")
            }

            // Verificar que el hash almacenado es correcto
            val hashCalculado = calcularHash(registro)
            if (registro.hashRegistro != hashCalculado) {
                errores.add("Registro ${registro.id}: hash almacenado no coincide con recalculado")
            }

            hashAnterior = registro.hashRegistro
        }

        return CadenaVerificacion(
            valida = errores.isEmpty(),
            totalRegistros = registros.size,
            errores = errores
        )
    }
}
