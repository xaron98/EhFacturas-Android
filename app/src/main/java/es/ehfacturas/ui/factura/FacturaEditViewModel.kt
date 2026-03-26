// app/src/main/java/es/ehfacturas/ui/factura/FacturaEditViewModel.kt
package es.ehfacturas.ui.factura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.*
import es.ehfacturas.data.repository.ArticuloRepository
import es.ehfacturas.data.repository.ClienteRepository
import es.ehfacturas.data.repository.FacturaRepository
import es.ehfacturas.data.repository.NegocioRepository
import es.ehfacturas.domain.validation.NifValidator
import es.ehfacturas.domain.validation.RecalculoTotales
import es.ehfacturas.domain.validation.TotalesFactura
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class FacturaEditUiState(
    val factura: Factura = Factura(),
    val lineas: List<LineaFactura> = emptyList(),
    val totales: TotalesFactura = TotalesFactura(0.0, 0.0, 0.0, 0.0, 0.0, emptyMap()),
    val esNueva: Boolean = true,
    val guardando: Boolean = false,
    val guardadoOk: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val aplicarIRPF: Boolean = false,
    val irpfPorcentaje: Double = 15.0
)

@HiltViewModel
class FacturaEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val facturaRepository: FacturaRepository,
    private val clienteRepository: ClienteRepository,
    private val articuloRepository: ArticuloRepository,
    private val negocioRepository: NegocioRepository
) : ViewModel() {

    private val facturaId: Long? = savedStateHandle.get<String>("facturaId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(FacturaEditUiState())
    val uiState: StateFlow<FacturaEditUiState> = _uiState.asStateFlow()

    // Para búsqueda de clientes
    private val _busquedaCliente = MutableStateFlow("")
    val busquedaCliente: StateFlow<String> = _busquedaCliente.asStateFlow()

    private val _clientesSugeridos = MutableStateFlow<List<Cliente>>(emptyList())
    val clientesSugeridos: StateFlow<List<Cliente>> = _clientesSugeridos.asStateFlow()

    // Para selector de artículos
    val articulos: StateFlow<List<Articulo>> = articuloRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val negocio = negocioRepository.obtenerNegocioSync()

            if (facturaId != null && facturaId > 0) {
                // Editar factura existente
                val factura = facturaRepository.obtenerPorId(facturaId) ?: return@launch
                val lineas = facturaRepository.obtenerLineasSync(facturaId)
                _uiState.value = FacturaEditUiState(
                    factura = factura,
                    lineas = lineas,
                    esNueva = false,
                    aplicarIRPF = negocio?.aplicarIRPF ?: false,
                    irpfPorcentaje = negocio?.irpfPorcentaje ?: 15.0
                )
            } else {
                // Nueva factura
                val numero = negocio?.generarNumeroFactura() ?: ""
                val vencimiento = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.time
                _uiState.value = FacturaEditUiState(
                    factura = Factura(
                        numeroFactura = numero,
                        fechaVencimiento = vencimiento
                    ),
                    esNueva = true,
                    aplicarIRPF = negocio?.aplicarIRPF ?: false,
                    irpfPorcentaje = negocio?.irpfPorcentaje ?: 15.0
                )
            }
            recalcularTotales()
        }
    }

    fun buscarClientes(texto: String) {
        _busquedaCliente.value = texto
        viewModelScope.launch {
            _clientesSugeridos.value = if (texto.length >= 2) {
                clienteRepository.buscar(texto)
            } else {
                emptyList()
            }
        }
    }

    fun setCliente(cliente: Cliente) {
        _uiState.update {
            it.copy(
                factura = it.factura.copy(
                    clienteId = cliente.id,
                    clienteNombre = cliente.nombre,
                    clienteNIF = cliente.nif,
                    clienteDireccion = buildString {
                        append(cliente.direccion)
                        if (cliente.codigoPostal.isNotEmpty()) append(", ${cliente.codigoPostal}")
                        if (cliente.ciudad.isNotEmpty()) append(" ${cliente.ciudad}")
                    }
                )
            )
        }
        _busquedaCliente.value = cliente.nombre
        _clientesSugeridos.value = emptyList()
    }

    fun limpiarCliente() {
        _uiState.update {
            it.copy(
                factura = it.factura.copy(
                    clienteId = null,
                    clienteNombre = "",
                    clienteNIF = "",
                    clienteDireccion = ""
                )
            )
        }
        _busquedaCliente.value = ""
    }

    fun setFecha(fecha: Date) {
        _uiState.update { it.copy(factura = it.factura.copy(fecha = fecha)) }
    }

    fun setFechaVencimiento(fecha: Date?) {
        _uiState.update { it.copy(factura = it.factura.copy(fechaVencimiento = fecha)) }
    }

    fun setTipoFactura(tipo: TipoFacturaVF) {
        _uiState.update { it.copy(factura = it.factura.copy(tipoFactura = tipo)) }
    }

    fun setObservaciones(texto: String) {
        _uiState.update { it.copy(factura = it.factura.copy(observaciones = texto)) }
    }

    fun setNotasInternas(texto: String) {
        _uiState.update { it.copy(factura = it.factura.copy(notasInternas = texto)) }
    }

    fun setDescuentoGlobal(porcentaje: Double) {
        _uiState.update { it.copy(factura = it.factura.copy(descuentoGlobalPorcentaje = porcentaje)) }
        recalcularTotales()
    }

    // --- Gestión de líneas ---

    fun addLinea() {
        _uiState.update {
            val nuevaLinea = LineaFactura(
                orden = it.lineas.size,
                porcentajeIVA = 21.0  // IVA por defecto
            )
            it.copy(lineas = it.lineas + nuevaLinea)
        }
    }

    fun addLineaDesdeArticulo(articulo: Articulo) {
        _uiState.update {
            val nuevaLinea = LineaFactura(
                orden = it.lineas.size,
                articuloId = articulo.id,
                referencia = articulo.referencia,
                concepto = articulo.nombre,
                cantidad = 1.0,
                unidad = articulo.unidad,
                precioUnitario = articulo.precioUnitario,
                porcentajeIVA = articulo.tipoIVA.porcentaje
            )
            it.copy(lineas = it.lineas + nuevaLinea)
        }
        recalcularTotales()
    }

    fun updateLinea(index: Int, linea: LineaFactura) {
        _uiState.update {
            val nuevasLineas = it.lineas.toMutableList()
            if (index in nuevasLineas.indices) {
                nuevasLineas[index] = linea.copy(subtotal = linea.calcularSubtotal())
            }
            it.copy(lineas = nuevasLineas)
        }
        recalcularTotales()
    }

    fun removeLinea(index: Int) {
        _uiState.update {
            val nuevasLineas = it.lineas.toMutableList()
            if (index in nuevasLineas.indices) {
                nuevasLineas.removeAt(index)
                // Reordenar
                nuevasLineas.forEachIndexed { i, l -> nuevasLineas[i] = l.copy(orden = i) }
            }
            it.copy(lineas = nuevasLineas)
        }
        recalcularTotales()
    }

    private fun recalcularTotales() {
        _uiState.update { state ->
            val totales = RecalculoTotales.calcular(
                lineas = state.lineas,
                descuentoGlobalPorcentaje = state.factura.descuentoGlobalPorcentaje,
                aplicarIRPF = state.aplicarIRPF,
                irpfPorcentaje = state.irpfPorcentaje
            )
            state.copy(
                totales = totales,
                factura = state.factura.copy(
                    baseImponible = totales.baseImponible,
                    totalIVA = totales.totalIVA,
                    totalIRPF = totales.totalIRPF,
                    totalFactura = totales.totalFactura
                )
            )
        }
    }

    // --- Guardar ---

    fun guardar() {
        val errores = validar()
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(guardando = true, errores = emptyMap()) }

        viewModelScope.launch {
            val state = _uiState.value
            val factura = state.factura.copy(fechaModificacion = Date())

            if (state.esNueva) {
                val facturaId = facturaRepository.guardar(factura)
                val lineasConId = state.lineas.map { it.copy(facturaId = facturaId) }
                facturaRepository.guardarLineas(lineasConId)
                // Incrementar número de factura del negocio
                val negocio = negocioRepository.obtenerNegocioSync()
                negocio?.let { negocioRepository.incrementarNumeroFactura(it.id) }
            } else {
                facturaRepository.actualizar(factura)
                facturaRepository.eliminarLineasDeFactura(factura.id)
                val lineasConId = state.lineas.map { it.copy(facturaId = factura.id) }
                facturaRepository.guardarLineas(lineasConId)
            }

            _uiState.update { it.copy(guardando = false, guardadoOk = true) }
        }
    }

    private fun validar(): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        val state = _uiState.value

        if (state.lineas.isEmpty()) {
            errores["lineas"] = "Añade al menos una línea"
        }

        state.lineas.forEachIndexed { i, linea ->
            if (linea.concepto.isBlank()) {
                errores["linea_${i}_concepto"] = "Concepto requerido"
            }
            if (linea.cantidad <= 0) {
                errores["linea_${i}_cantidad"] = "Cantidad debe ser > 0"
            }
            if (linea.precioUnitario < 0) {
                errores["linea_${i}_precio"] = "Precio no puede ser negativo"
            }
        }

        return errores
    }
}
