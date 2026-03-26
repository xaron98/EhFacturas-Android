package es.ehfacturas.ai

import es.ehfacturas.data.db.entity.*
import es.ehfacturas.data.repository.*
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolExecutor @Inject constructor(
    private val negocioRepository: NegocioRepository,
    private val clienteRepository: ClienteRepository,
    private val articuloRepository: ArticuloRepository,
    private val facturaRepository: FacturaRepository,
    private val gastoRepository: GastoRepository
) {
    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    suspend fun executeTool(name: String, arguments: Map<String, Any?>): String {
        return try {
            when (name) {
                "configurar_negocio" -> configurarNegocio(arguments)
                "crear_cliente" -> crearCliente(arguments)
                "buscar_cliente" -> buscarCliente(arguments)
                "crear_articulo" -> crearArticulo(arguments)
                "buscar_articulo" -> buscarArticulo(arguments)
                "crear_factura" -> crearFactura(arguments)
                "marcar_pagada" -> marcarPagada(arguments)
                "anular_factura" -> anularFactura(arguments)
                "registrar_gasto" -> registrarGasto(arguments)
                "consultar_resumen" -> consultarResumen(arguments)
                else -> "Herramienta desconocida: $name"
            }
        } catch (e: Exception) {
            "Error ejecutando $name: ${e.message}"
        }
    }

    private suspend fun configurarNegocio(args: Map<String, Any?>): String {
        val negocio = negocioRepository.obtenerNegocioSync()
        val actualizado = (negocio ?: Negocio()).copy(
            nombre = args["nombre"] as? String ?: negocio?.nombre ?: "",
            nif = args["nif"] as? String ?: negocio?.nif ?: "",
            telefono = args["telefono"] as? String ?: negocio?.telefono ?: "",
            email = args["email"] as? String ?: negocio?.email ?: "",
            direccion = args["direccion"] as? String ?: negocio?.direccion ?: "",
            codigoPostal = args["codigoPostal"] as? String ?: negocio?.codigoPostal ?: "",
            ciudad = args["ciudad"] as? String ?: negocio?.ciudad ?: "",
            provincia = args["provincia"] as? String ?: negocio?.provincia ?: ""
        )
        negocioRepository.guardar(actualizado)
        return "Negocio configurado: ${actualizado.nombre}"
    }

    private suspend fun crearCliente(args: Map<String, Any?>): String {
        val cliente = Cliente(
            nombre = args["nombre"] as? String ?: "",
            nif = args["nif"] as? String ?: "",
            email = args["email"] as? String ?: "",
            telefono = args["telefono"] as? String ?: "",
            direccion = args["direccion"] as? String ?: "",
            codigoPostal = args["codigoPostal"] as? String ?: "",
            ciudad = args["ciudad"] as? String ?: "",
            provincia = args["provincia"] as? String ?: ""
        )
        val id = clienteRepository.guardar(cliente)
        return "Cliente creado: ${cliente.nombre} (ID: $id)"
    }

    private suspend fun buscarCliente(args: Map<String, Any?>): String {
        val texto = args["texto"] as? String ?: return "Texto de búsqueda vacío"
        val resultados = clienteRepository.buscar(texto)
        if (resultados.isEmpty()) return "No se encontraron clientes con '$texto'"
        return resultados.joinToString("\n") { "- ${it.nombre} (NIF: ${it.nif})" }
    }

    private suspend fun crearArticulo(args: Map<String, Any?>): String {
        val unidad = when (args["unidad"] as? String) {
            "h" -> UnidadMedida.HORA
            "m" -> UnidadMedida.METRO
            "kg" -> UnidadMedida.KILOGRAMO
            "l" -> UnidadMedida.LITRO
            "servicio" -> UnidadMedida.SERVICIO
            else -> UnidadMedida.UNIDAD
        }
        val tipoIVA = when (args["iva"] as? String) {
            "reducido" -> TipoIVA.REDUCIDO
            "superReducido" -> TipoIVA.SUPER_REDUCIDO
            "exento" -> TipoIVA.EXENTO
            else -> TipoIVA.GENERAL
        }
        val articulo = Articulo(
            nombre = args["nombre"] as? String ?: "",
            precioUnitario = (args["precio"] as? Number)?.toDouble() ?: 0.0,
            unidad = unidad,
            tipoIVA = tipoIVA,
            descripcion = args["descripcion"] as? String ?: ""
        )
        val id = articuloRepository.guardar(articulo)
        return "Artículo creado: ${articulo.nombre} a ${formatoMoneda.format(articulo.precioUnitario)} (ID: $id)"
    }

    private suspend fun buscarArticulo(args: Map<String, Any?>): String {
        val texto = args["texto"] as? String ?: return "Texto de búsqueda vacío"
        val resultados = articuloRepository.buscar(texto)
        if (resultados.isEmpty()) return "No se encontraron artículos con '$texto'"
        return resultados.joinToString("\n") {
            "- ${it.nombre}: ${formatoMoneda.format(it.precioUnitario)}/${it.unidad.abreviatura}"
        }
    }

    private suspend fun crearFactura(args: Map<String, Any?>): String {
        val nombreCliente = args["nombreCliente"] as? String ?: ""
        val articulosTexto = args["articulosTexto"] as? String ?: ""
        val descuento = (args["descuento"] as? Number)?.toDouble() ?: 0.0
        val observaciones = args["observaciones"] as? String ?: ""
        val esPresupuesto = args["esPresupuesto"] as? Boolean ?: false

        // Buscar cliente
        val clientes = clienteRepository.buscar(nombreCliente)
        val cliente = clientes.firstOrNull()

        // Obtener negocio para número de factura
        val negocio = negocioRepository.obtenerNegocioSync()
        val numero = negocio?.generarNumeroFactura() ?: "FAC-0001"

        val estado = if (esPresupuesto) EstadoFactura.PRESUPUESTO else EstadoFactura.BORRADOR

        val factura = Factura(
            numeroFactura = numero,
            estado = estado,
            clienteId = cliente?.id,
            clienteNombre = cliente?.nombre ?: nombreCliente,
            clienteNIF = cliente?.nif ?: "",
            clienteDireccion = cliente?.let { "${it.direccion}, ${it.codigoPostal} ${it.ciudad}" } ?: "",
            descuentoGlobalPorcentaje = descuento,
            observaciones = observaciones,
            promptOriginal = articulosTexto
        )

        val facturaId = facturaRepository.guardar(factura)

        // Incrementar número
        negocio?.let { negocioRepository.incrementarNumeroFactura(it.id) }

        // Parsear artículos del texto y crear líneas
        val lineas = parsearArticulosTexto(articulosTexto)
        var totalBase = 0.0
        var totalIVA = 0.0

        lineas.forEachIndexed { index, linea ->
            val lineaFactura = linea.copy(facturaId = facturaId, orden = index)
            facturaRepository.guardarLinea(lineaFactura)
            val sub = lineaFactura.calcularSubtotal()
            totalBase += sub
            totalIVA += sub * (lineaFactura.porcentajeIVA / 100.0)
        }

        // Aplicar descuento
        val baseConDescuento = totalBase * (1 - descuento / 100.0)
        val ivaConDescuento = totalIVA * (1 - descuento / 100.0)
        val total = baseConDescuento + ivaConDescuento

        // Actualizar totales
        facturaRepository.actualizar(
            factura.copy(
                id = facturaId,
                baseImponible = baseConDescuento,
                totalIVA = ivaConDescuento,
                totalFactura = total
            )
        )

        val tipo = if (esPresupuesto) "Presupuesto" else "Factura"
        return "$tipo $numero creada para $nombreCliente. Total: ${formatoMoneda.format(total)} [ID:$facturaId]"
    }

    private suspend fun parsearArticulosTexto(texto: String): List<LineaFactura> {
        // Parsear texto como "2 horas diseño a 50€, 1 logo a 200€"
        val lineas = mutableListOf<LineaFactura>()
        val partes = texto.split(",", ";", " y ")

        for (parte in partes) {
            val trimmed = parte.trim()
            if (trimmed.isEmpty()) continue

            // Intentar parsear: [cantidad] [concepto] [a precio€]
            val precioMatch = Regex("(\\d+[.,]?\\d*)\\s*€").find(trimmed)
            val cantidadMatch = Regex("^(\\d+[.,]?\\d*)\\s+").find(trimmed)

            val precio = precioMatch?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            val cantidad = cantidadMatch?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull() ?: 1.0

            // Extraer concepto (quitar cantidad del inicio y precio del final)
            var concepto = trimmed
            cantidadMatch?.let { concepto = concepto.removePrefix(it.value).trim() }
            precioMatch?.let { concepto = concepto.replace(Regex("\\s*(a|@)\\s*${Regex.escape(it.value)}"), "").trim() }
            concepto = concepto.replace(Regex("\\s*(a|@)\\s*$"), "").trim()

            if (concepto.isEmpty()) concepto = trimmed

            // Buscar artículo existente
            val articulosExistentes = articuloRepository.buscar(concepto)
            val articuloExistente = articulosExistentes.firstOrNull()

            val precioFinal = if (precio > 0) precio else articuloExistente?.precioUnitario ?: 0.0
            val iva = articuloExistente?.tipoIVA?.porcentaje ?: 21.0

            lineas.add(
                LineaFactura(
                    concepto = articuloExistente?.nombre ?: concepto,
                    cantidad = cantidad,
                    precioUnitario = precioFinal,
                    porcentajeIVA = iva,
                    subtotal = cantidad * precioFinal,
                    articuloId = articuloExistente?.id,
                    unidad = articuloExistente?.unidad ?: UnidadMedida.UNIDAD
                )
            )
        }

        return lineas
    }

    private suspend fun marcarPagada(args: Map<String, Any?>): String {
        val texto = args["numeroFactura"] as? String ?: return "Falta número de factura"
        val facturas = facturaRepository.buscar(texto)
        val factura = facturas.firstOrNull() ?: return "No se encontró factura '$texto'"
        facturaRepository.actualizar(factura.copy(estado = EstadoFactura.PAGADA))
        return "Factura ${factura.numeroFactura} marcada como pagada"
    }

    private suspend fun anularFactura(args: Map<String, Any?>): String {
        val texto = args["numeroFactura"] as? String ?: return "Falta número de factura"
        val facturas = facturaRepository.buscar(texto)
        val factura = facturas.firstOrNull() ?: return "No se encontró factura '$texto'"
        facturaRepository.actualizar(factura.copy(estado = EstadoFactura.ANULADA))
        return "Factura ${factura.numeroFactura} anulada"
    }

    private suspend fun registrarGasto(args: Map<String, Any?>): String {
        val gasto = Gasto(
            concepto = args["concepto"] as? String ?: "",
            importe = (args["importe"] as? Number)?.toDouble() ?: 0.0,
            categoria = args["categoria"] as? String ?: "otros",
            proveedor = args["proveedor"] as? String ?: ""
        )
        val id = gastoRepository.guardar(gasto)
        return "Gasto registrado: ${gasto.concepto} por ${formatoMoneda.format(gasto.importe)} (ID: $id)"
    }

    private suspend fun consultarResumen(args: Map<String, Any?>): String {
        val tipo = args["tipo"] as? String ?: "todo"
        val sb = StringBuilder()

        if (tipo == "facturacion" || tipo == "todo") {
            val facturas = facturaRepository.obtenerTodas().first()
            val emitidas = facturas.filter { it.estado == EstadoFactura.EMITIDA || it.estado == EstadoFactura.PAGADA }
            val total = emitidas.sumOf { it.totalFactura }
            val pendientes = facturas.count { it.estado == EstadoFactura.EMITIDA }
            sb.appendLine("📊 Facturación: ${facturas.size} facturas, ${formatoMoneda.format(total)} total, $pendientes pendientes")
        }
        if (tipo == "clientes" || tipo == "todo") {
            val clientes = clienteRepository.obtenerTodos().first()
            sb.appendLine("👥 Clientes: ${clientes.size} activos")
        }
        if (tipo == "articulos" || tipo == "todo") {
            val articulos = articuloRepository.obtenerTodos().first()
            sb.appendLine("📦 Artículos: ${articulos.size} activos")
        }
        if (tipo == "gastos" || tipo == "todo") {
            val gastos = gastoRepository.obtenerTodos().first()
            val totalGastos = gastos.sumOf { it.importe }
            sb.appendLine("💰 Gastos: ${gastos.size} registros, ${formatoMoneda.format(totalGastos)} total")
        }

        return sb.toString().ifEmpty { "No hay datos disponibles" }
    }
}
