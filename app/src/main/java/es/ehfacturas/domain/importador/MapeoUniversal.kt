package es.ehfacturas.domain.importador

object MapeoUniversal {

    // Mapeo de sinonimos para columnas de articulos
    private val sinonimosArticulos = mapOf(
        "nombre" to listOf("nombre", "descripcion", "articulo", "producto", "concepto", "item", "name", "description"),
        "referencia" to listOf("referencia", "ref", "codigo", "code", "sku", "reference"),
        "precioUnitario" to listOf("precio", "pvp", "price", "importe", "precio venta", "precio unitario", "unit price"),
        "precioCoste" to listOf("coste", "precio coste", "cost", "precio compra"),
        "unidad" to listOf("unidad", "ud", "unit", "medida", "unidad medida"),
        "proveedor" to listOf("proveedor", "supplier", "vendor", "fabricante"),
        "categoria" to listOf("categoria", "familia", "grupo", "category", "group"),
        "tipoIVA" to listOf("iva", "tipo iva", "vat", "impuesto", "tax")
    )

    // Mapeo de sinonimos para columnas de clientes
    private val sinonimosClientes = mapOf(
        "nombre" to listOf("nombre", "razon social", "cliente", "name", "company", "empresa"),
        "nif" to listOf("nif", "cif", "nie", "dni", "vat", "tax id", "identificacion fiscal"),
        "direccion" to listOf("direccion", "domicilio", "address", "calle"),
        "codigoPostal" to listOf("cp", "codigo postal", "postal", "zip", "postal code"),
        "ciudad" to listOf("ciudad", "localidad", "poblacion", "city", "town"),
        "provincia" to listOf("provincia", "estado", "region", "province", "state"),
        "telefono" to listOf("telefono", "tel", "phone", "movil", "mobile"),
        "email" to listOf("email", "correo", "e-mail", "mail", "correo electronico")
    )

    data class ResultadoMapeo(
        val mapeo: Map<String, Int>,  // campo -> indice columna
        val confianza: Double
    )

    fun detectar(cabeceras: List<String>, tipo: String): ResultadoMapeo {
        val sinonimos = if (tipo == "articulos") sinonimosArticulos else sinonimosClientes
        val mapeo = mutableMapOf<String, Int>()
        var matches = 0

        sinonimos.forEach { (campo, variantes) ->
            cabeceras.forEachIndexed { index, cabecera ->
                val normalizada = cabecera.lowercase().trim()
                if (variantes.any { normalizada.contains(it) }) {
                    if (!mapeo.containsKey(campo)) {
                        mapeo[campo] = index
                        matches++
                    }
                }
            }
        }

        val confianza = if (sinonimos.isNotEmpty()) matches.toDouble() / sinonimos.size else 0.0
        return ResultadoMapeo(mapeo, confianza)
    }

    fun obtenerValor(fila: List<String>, mapeo: Map<String, Int>, campo: String): String {
        val index = mapeo[campo] ?: return ""
        return if (index < fila.size) fila[index].trim() else ""
    }
}
