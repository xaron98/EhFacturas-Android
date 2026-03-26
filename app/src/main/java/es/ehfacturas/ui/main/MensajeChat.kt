package es.ehfacturas.ui.main

import java.util.Date
import java.util.UUID

enum class TipoMensaje {
    USUARIO,
    IA,
    ERROR,
    SISTEMA,
    FACTURA
}

data class MensajeChat(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val tipo: TipoMensaje,
    val texto: String,
    val facturaId: Long? = null,
    val toolUsed: String? = null
)
