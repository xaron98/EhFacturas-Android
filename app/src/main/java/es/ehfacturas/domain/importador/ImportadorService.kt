package es.ehfacturas.domain.importador

import es.ehfacturas.data.db.entity.Articulo
import es.ehfacturas.data.db.entity.Cliente
import es.ehfacturas.data.db.entity.TipoIVA
import es.ehfacturas.data.db.entity.UnidadMedida
import es.ehfacturas.data.repository.ArticuloRepository
import es.ehfacturas.data.repository.ClienteRepository
import es.ehfacturas.domain.validation.Formateadores
import javax.inject.Inject
import javax.inject.Singleton

data class ResultadoImportacion(
    val importados: Int,
    val duplicados: Int,
    val errores: Int,
    val mensajes: List<String>
)

@Singleton
class ImportadorService @Inject constructor(
    private val articuloRepository: ArticuloRepository,
    private val clienteRepository: ClienteRepository
) {
    suspend fun importarArticulos(
        filas: List<List<String>>,
        mapeo: Map<String, Int>
    ): ResultadoImportacion {
        var importados = 0
        var duplicados = 0
        var errores = 0
        val mensajes = mutableListOf<String>()

        val existentes = articuloRepository.obtenerTodosSync()
        val referenciasExistentes = existentes.map { it.referencia.lowercase() }.toSet()
        val nombresExistentes = existentes.map { it.nombre.lowercase() }.toSet()

        for (fila in filas) {
            try {
                val nombre = MapeoUniversal.obtenerValor(fila, mapeo, "nombre")
                if (nombre.isBlank()) { errores++; continue }

                val referencia = MapeoUniversal.obtenerValor(fila, mapeo, "referencia")

                // Duplicado por referencia o nombre
                if (referencia.isNotBlank() && referencia.lowercase() in referenciasExistentes) {
                    duplicados++; continue
                }
                if (nombre.lowercase() in nombresExistentes) {
                    duplicados++; continue
                }

                val precioTexto = MapeoUniversal.obtenerValor(fila, mapeo, "precioUnitario")
                val precio = Formateadores.parsearPrecio(precioTexto) ?: 0.0

                val precioCosteTexto = MapeoUniversal.obtenerValor(fila, mapeo, "precioCoste")
                val precioCoste = Formateadores.parsearPrecio(precioCosteTexto) ?: 0.0

                val unidadTexto = MapeoUniversal.obtenerValor(fila, mapeo, "unidad").lowercase()
                val unidad = UnidadMedida.desdeAbreviatura(unidadTexto) ?: UnidadMedida.UNIDAD

                val ivaTexto = MapeoUniversal.obtenerValor(fila, mapeo, "tipoIVA").lowercase()
                val tipoIVA = when {
                    "reducido" in ivaTexto || "10" in ivaTexto -> TipoIVA.REDUCIDO
                    "super" in ivaTexto || "4" in ivaTexto -> TipoIVA.SUPER_REDUCIDO
                    "exento" in ivaTexto || "0" in ivaTexto -> TipoIVA.EXENTO
                    else -> TipoIVA.GENERAL
                }

                val articulo = Articulo(
                    nombre = nombre,
                    referencia = referencia,
                    precioUnitario = precio,
                    precioCoste = precioCoste,
                    unidad = unidad,
                    tipoIVA = tipoIVA,
                    proveedor = MapeoUniversal.obtenerValor(fila, mapeo, "proveedor"),
                    descripcion = MapeoUniversal.obtenerValor(fila, mapeo, "categoria")
                )
                articuloRepository.guardar(articulo)
                importados++
            } catch (e: Exception) {
                errores++
                mensajes.add("Error fila: ${e.message}")
            }
        }

        mensajes.add(0, "$importados importados, $duplicados duplicados, $errores errores")
        return ResultadoImportacion(importados, duplicados, errores, mensajes)
    }

    suspend fun importarClientes(
        filas: List<List<String>>,
        mapeo: Map<String, Int>
    ): ResultadoImportacion {
        var importados = 0
        var duplicados = 0
        var errores = 0
        val mensajes = mutableListOf<String>()

        val existentes = clienteRepository.obtenerTodosSync()
        val nifsExistentes = existentes.map { it.nif.lowercase() }.filter { it.isNotEmpty() }.toSet()
        val nombresExistentes = existentes.map { it.nombre.lowercase() }.toSet()

        for (fila in filas) {
            try {
                val nombre = MapeoUniversal.obtenerValor(fila, mapeo, "nombre")
                if (nombre.isBlank()) { errores++; continue }

                val nif = MapeoUniversal.obtenerValor(fila, mapeo, "nif")

                if (nif.isNotBlank() && nif.lowercase() in nifsExistentes) {
                    duplicados++; continue
                }
                if (nombre.lowercase() in nombresExistentes) {
                    duplicados++; continue
                }

                val cliente = Cliente(
                    nombre = nombre,
                    nif = nif,
                    direccion = MapeoUniversal.obtenerValor(fila, mapeo, "direccion"),
                    codigoPostal = MapeoUniversal.obtenerValor(fila, mapeo, "codigoPostal"),
                    ciudad = MapeoUniversal.obtenerValor(fila, mapeo, "ciudad"),
                    provincia = MapeoUniversal.obtenerValor(fila, mapeo, "provincia"),
                    telefono = MapeoUniversal.obtenerValor(fila, mapeo, "telefono"),
                    email = MapeoUniversal.obtenerValor(fila, mapeo, "email")
                )
                clienteRepository.guardar(cliente)
                importados++
            } catch (e: Exception) {
                errores++
                mensajes.add("Error fila: ${e.message}")
            }
        }

        mensajes.add(0, "$importados importados, $duplicados duplicados, $errores errores")
        return ResultadoImportacion(importados, duplicados, errores, mensajes)
    }
}
