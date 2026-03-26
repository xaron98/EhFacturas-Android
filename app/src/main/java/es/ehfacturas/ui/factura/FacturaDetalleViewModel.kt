// app/src/main/java/es/ehfacturas/ui/factura/FacturaDetalleViewModel.kt
package es.ehfacturas.ui.factura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.*
import es.ehfacturas.data.repository.FacturaRepository
import es.ehfacturas.data.repository.NegocioRepository
import es.ehfacturas.data.repository.VeriFactuRepository
import es.ehfacturas.domain.pdf.FacturaPdfGenerator
import es.ehfacturas.domain.validation.RecalculoTotales
import es.ehfacturas.domain.validation.TotalesFactura
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class FacturaDetalleUiState(
    val factura: Factura? = null,
    val lineas: List<LineaFactura> = emptyList(),
    val totales: TotalesFactura = TotalesFactura(0.0, 0.0, 0.0, 0.0, 0.0, emptyMap()),
    val registros: List<RegistroFacturacion> = emptyList(),
    val negocio: Negocio? = null,
    val cargando: Boolean = true,
    val esEditable: Boolean = false,
    val aplicarIRPF: Boolean = false,
    val irpfPorcentaje: Double = 15.0,
    val mensaje: String? = null,
    val facturaIdNueva: Long? = null  // Para navegar a factura duplicada/rectificativa
)

@HiltViewModel
class FacturaDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val facturaRepository: FacturaRepository,
    private val negocioRepository: NegocioRepository,
    private val veriFactuRepository: VeriFactuRepository,
    private val pdfGenerator: FacturaPdfGenerator
) : ViewModel() {

    private val facturaId: Long = savedStateHandle.get<String>("facturaId")?.toLong() ?: 0L

    private val _uiState = MutableStateFlow(FacturaDetalleUiState())
    val uiState: StateFlow<FacturaDetalleUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val factura = facturaRepository.obtenerPorId(facturaId)
            val lineas = facturaRepository.obtenerLineasSync(facturaId)
            val negocio = negocioRepository.obtenerNegocioSync()

            if (factura != null) {
                val totales = RecalculoTotales.calcular(
                    lineas, factura.descuentoGlobalPorcentaje,
                    negocio?.aplicarIRPF ?: false, negocio?.irpfPorcentaje ?: 15.0
                )
                _uiState.value = FacturaDetalleUiState(
                    factura = factura,
                    lineas = lineas,
                    totales = totales,
                    negocio = negocio,
                    cargando = false,
                    esEditable = factura.estado == EstadoFactura.BORRADOR || factura.estado == EstadoFactura.PRESUPUESTO,
                    aplicarIRPF = negocio?.aplicarIRPF ?: false,
                    irpfPorcentaje = negocio?.irpfPorcentaje ?: 15.0
                )
            }
        }
        // Registros reactivos
        viewModelScope.launch {
            veriFactuRepository.obtenerRegistrosPorFactura(facturaId).collect { registros ->
                _uiState.update { it.copy(registros = registros) }
            }
        }
    }

    fun emitir() {
        viewModelScope.launch {
            val state = _uiState.value
            val factura = state.factura ?: return@launch
            val negocio = state.negocio ?: return@launch

            if (factura.estado != EstadoFactura.BORRADOR) {
                _uiState.update { it.copy(mensaje = "Solo se pueden emitir borradores") }
                return@launch
            }
            if (state.lineas.isEmpty()) {
                _uiState.update { it.copy(mensaje = "La factura no tiene líneas") }
                return@launch
            }

            // La generación de hash se hará en Task de Fase 4
            val facturaEmitida = factura.copy(
                estado = EstadoFactura.EMITIDA,
                fechaModificacion = Date()
            )
            facturaRepository.actualizar(facturaEmitida)
            _uiState.update { it.copy(factura = facturaEmitida, esEditable = false, mensaje = "Factura emitida") }
        }
    }

    fun cobrar() {
        viewModelScope.launch {
            val factura = _uiState.value.factura ?: return@launch
            if (factura.estado != EstadoFactura.EMITIDA) return@launch
            val cobrada = factura.copy(estado = EstadoFactura.PAGADA, fechaModificacion = Date())
            facturaRepository.actualizar(cobrada)
            _uiState.update { it.copy(factura = cobrada, mensaje = "Factura cobrada") }
        }
    }

    fun anular() {
        viewModelScope.launch {
            val factura = _uiState.value.factura ?: return@launch
            if (factura.estado != EstadoFactura.EMITIDA && factura.estado != EstadoFactura.PAGADA) return@launch
            val anulada = factura.copy(estado = EstadoFactura.ANULADA, fechaModificacion = Date())
            facturaRepository.actualizar(anulada)
            _uiState.update { it.copy(factura = anulada, esEditable = false, mensaje = "Factura anulada") }
        }
    }

    fun convertirEnFactura() {
        viewModelScope.launch {
            val factura = _uiState.value.factura ?: return@launch
            if (factura.estado != EstadoFactura.PRESUPUESTO) return@launch
            val borrador = factura.copy(estado = EstadoFactura.BORRADOR, fechaModificacion = Date())
            facturaRepository.actualizar(borrador)
            _uiState.update { it.copy(factura = borrador, esEditable = true, mensaje = "Convertida en factura") }
        }
    }

    fun duplicar() {
        viewModelScope.launch {
            val state = _uiState.value
            val factura = state.factura ?: return@launch
            val negocio = state.negocio ?: return@launch

            val nuevaFactura = factura.copy(
                id = 0,
                numeroFactura = negocio.generarNumeroFactura(),
                estado = EstadoFactura.BORRADOR,
                fecha = Date(),
                fechaVencimiento = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.time,
                fechaCreacion = Date(),
                fechaModificacion = Date(),
                pdfRuta = null,
                promptOriginal = null
            )
            val nuevoId = facturaRepository.guardar(nuevaFactura)
            val nuevasLineas = state.lineas.map { it.copy(id = 0, facturaId = nuevoId) }
            facturaRepository.guardarLineas(nuevasLineas)
            negocioRepository.incrementarNumeroFactura(negocio.id)
            _uiState.update { it.copy(facturaIdNueva = nuevoId, mensaje = "Factura duplicada") }
        }
    }

    fun generarPdf() {
        viewModelScope.launch {
            val state = _uiState.value
            val factura = state.factura ?: return@launch
            val registro = state.registros.firstOrNull()

            val archivo = pdfGenerator.generar(factura, state.lineas, registro)
            val facturaConPdf = factura.copy(pdfRuta = archivo.absolutePath)
            facturaRepository.actualizar(facturaConPdf)
            _uiState.update { it.copy(factura = facturaConPdf, mensaje = "PDF generado") }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun limpiarNavegacion() {
        _uiState.update { it.copy(facturaIdNueva = null) }
    }
}
