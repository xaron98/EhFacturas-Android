package es.ehfacturas.data.db.entity

// Unidad de medida para artículos
enum class UnidadMedida(val abreviatura: String, val descripcion: String) {
    UNIDAD("ud", "Unidad"),
    METRO("m", "Metro"),
    METRO_CUADRADO("m²", "Metro cuadrado"),
    HORA("h", "Hora"),
    KILOGRAMO("kg", "Kilogramo"),
    LITRO("l", "Litro"),
    ROLLO("rollo", "Rollo"),
    CAJA("caja", "Caja"),
    SERVICIO("servicio", "Servicio");

    companion object {
        fun desdeAbreviatura(abreviatura: String): UnidadMedida? =
            entries.find { it.abreviatura == abreviatura }
    }
}

// Tipo de IVA aplicable
enum class TipoIVA(val porcentaje: Double, val descripcion: String) {
    GENERAL(21.0, "General (21%)"),
    REDUCIDO(10.0, "Reducido (10%)"),
    SUPER_REDUCIDO(4.0, "Superreducido (4%)"),
    EXENTO(0.0, "Exento (0%)")
}

// Estado de una factura
enum class EstadoFactura(val descripcion: String, val color: String) {
    PRESUPUESTO("Presupuesto", "purple"),
    BORRADOR("Borrador", "gray"),
    EMITIDA("Emitida", "blue"),
    PAGADA("Pagada", "green"),
    VENCIDA("Vencida", "red"),
    ANULADA("Anulada", "orange")
}

// VeriFactu: tipo de registro
enum class TipoRegistro {
    ALTA,
    ANULACION
}

// VeriFactu: tipo de factura
enum class TipoFacturaVF {
    COMPLETA,
    SIMPLIFICADA,
    RECTIFICATIVA
}

// VeriFactu: estado de envío
enum class EstadoEnvioVF(val descripcion: String) {
    NO_ENVIADO("No enviado"),
    PENDIENTE("Pendiente"),
    ENVIADO("Enviado"),
    RECHAZADO("Rechazado"),
    ERROR("Error")
}

// Tipo de importación CSV
enum class TipoImportacion {
    ARTICULOS,
    CLIENTES
}
