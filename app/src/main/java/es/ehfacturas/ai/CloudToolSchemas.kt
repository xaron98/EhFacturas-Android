package es.ehfacturas.ai

object CloudToolSchemas {

    fun commandTools(): List<Map<String, Any>> = listOf(
        mapOf(
            "name" to "configurar_negocio",
            "description" to "Configura los datos del negocio/autónomo",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "nombre" to mapOf("type" to "string", "description" to "Nombre del negocio"),
                    "nif" to mapOf("type" to "string", "description" to "NIF/CIF"),
                    "telefono" to mapOf("type" to "string"),
                    "email" to mapOf("type" to "string"),
                    "direccion" to mapOf("type" to "string"),
                    "codigoPostal" to mapOf("type" to "string"),
                    "ciudad" to mapOf("type" to "string"),
                    "provincia" to mapOf("type" to "string")
                ),
                "required" to listOf("nombre")
            )
        ),
        mapOf(
            "name" to "crear_cliente",
            "description" to "Crea un nuevo cliente",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "nombre" to mapOf("type" to "string", "description" to "Nombre del cliente"),
                    "nif" to mapOf("type" to "string"),
                    "email" to mapOf("type" to "string"),
                    "telefono" to mapOf("type" to "string"),
                    "direccion" to mapOf("type" to "string"),
                    "codigoPostal" to mapOf("type" to "string"),
                    "ciudad" to mapOf("type" to "string"),
                    "provincia" to mapOf("type" to "string")
                ),
                "required" to listOf("nombre")
            )
        ),
        mapOf(
            "name" to "buscar_cliente",
            "description" to "Busca clientes por nombre o NIF",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "texto" to mapOf("type" to "string", "description" to "Texto de búsqueda")
                ),
                "required" to listOf("texto")
            )
        ),
        mapOf(
            "name" to "crear_articulo",
            "description" to "Crea un nuevo artículo o servicio",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "nombre" to mapOf("type" to "string", "description" to "Nombre del artículo"),
                    "precio" to mapOf("type" to "number", "description" to "Precio unitario sin IVA"),
                    "unidad" to mapOf("type" to "string", "description" to "Unidad: ud, h, m, kg, l, servicio"),
                    "iva" to mapOf("type" to "string", "description" to "Tipo IVA: general, reducido, superReducido, exento"),
                    "descripcion" to mapOf("type" to "string")
                ),
                "required" to listOf("nombre", "precio")
            )
        ),
        mapOf(
            "name" to "buscar_articulo",
            "description" to "Busca artículos por nombre o referencia",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "texto" to mapOf("type" to "string", "description" to "Texto de búsqueda")
                ),
                "required" to listOf("texto")
            )
        ),
        mapOf(
            "name" to "crear_factura",
            "description" to "Crea una factura borrador con líneas de artículos. Usa artículos existentes o crea nuevos.",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "nombreCliente" to mapOf("type" to "string", "description" to "Nombre del cliente"),
                    "articulosTexto" to mapOf("type" to "string", "description" to "Descripción de artículos. Ej: '2 horas de diseño a 50€, 1 logo a 200€'"),
                    "descuento" to mapOf("type" to "number", "description" to "Descuento global en %"),
                    "observaciones" to mapOf("type" to "string"),
                    "esPresupuesto" to mapOf("type" to "boolean", "description" to "true si es presupuesto en vez de factura")
                ),
                "required" to listOf("nombreCliente", "articulosTexto")
            )
        ),
        mapOf(
            "name" to "marcar_pagada",
            "description" to "Marca una factura como pagada",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "numeroFactura" to mapOf("type" to "string", "description" to "Número de factura o nombre del cliente")
                ),
                "required" to listOf("numeroFactura")
            )
        ),
        mapOf(
            "name" to "anular_factura",
            "description" to "Anula una factura",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "numeroFactura" to mapOf("type" to "string", "description" to "Número de factura")
                ),
                "required" to listOf("numeroFactura")
            )
        ),
        mapOf(
            "name" to "registrar_gasto",
            "description" to "Registra un gasto",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "concepto" to mapOf("type" to "string", "description" to "Concepto del gasto"),
                    "importe" to mapOf("type" to "number", "description" to "Importe en euros"),
                    "categoria" to mapOf("type" to "string", "description" to "Categoría: material, herramientas, vehiculo, oficina, otros"),
                    "proveedor" to mapOf("type" to "string")
                ),
                "required" to listOf("concepto", "importe")
            )
        ),
        mapOf(
            "name" to "consultar_resumen",
            "description" to "Consulta un resumen de facturación, clientes o artículos",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "tipo" to mapOf("type" to "string", "description" to "Tipo: facturacion, clientes, articulos, gastos, todo")
                ),
                "required" to listOf("tipo")
            )
        )
    )

    fun editTools(): List<Map<String, Any>> = listOf(
        mapOf(
            "name" to "modificar_linea",
            "description" to "Modifica una línea existente de la factura",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "indice" to mapOf("type" to "integer", "description" to "Índice de la línea (empezando en 1)"),
                    "cantidad" to mapOf("type" to "number"),
                    "precio" to mapOf("type" to "number"),
                    "concepto" to mapOf("type" to "string")
                ),
                "required" to listOf("indice")
            )
        ),
        mapOf(
            "name" to "anadir_linea",
            "description" to "Añade una nueva línea a la factura",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "concepto" to mapOf("type" to "string", "description" to "Nombre o descripción del artículo"),
                    "cantidad" to mapOf("type" to "number"),
                    "precio" to mapOf("type" to "number", "description" to "Precio unitario"),
                    "iva" to mapOf("type" to "number", "description" to "Porcentaje de IVA")
                ),
                "required" to listOf("concepto", "precio")
            )
        ),
        mapOf(
            "name" to "eliminar_linea",
            "description" to "Elimina una línea de la factura",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "indice" to mapOf("type" to "integer", "description" to "Índice de la línea (empezando en 1)")
                ),
                "required" to listOf("indice")
            )
        ),
        mapOf(
            "name" to "cambiar_descuento",
            "description" to "Cambia el descuento global de la factura",
            "input_schema" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "porcentaje" to mapOf("type" to "number", "description" to "Porcentaje de descuento (0-100)")
                ),
                "required" to listOf("porcentaje")
            )
        )
    )
}
