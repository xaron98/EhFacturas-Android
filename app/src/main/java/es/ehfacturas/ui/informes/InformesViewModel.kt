package es.ehfacturas.ui.informes

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.repository.FacturaRepository
import es.ehfacturas.data.repository.GastoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class Periodo(val descripcion: String) {
    MES("Este mes"),
    TRIMESTRE("Trimestre"),
    ANO("Este año"),
    TODO("Todo")
}

data class DatoMensual(
    val mes: String,
    val facturado: Double,
    val cobrado: Double
)

data class InformesUiState(
    val periodo: Periodo = Periodo.MES,
    val facturado: Double = 0.0,
    val cobrado: Double = 0.0,
    val pendiente: Double = 0.0,
    val ivaRepercutido: Double = 0.0,
    val irpfRetenido: Double = 0.0,
    val gastosPeriodo: Double = 0.0,
    val beneficioNeto: Double = 0.0,
    val beneficioReal: Double = 0.0,
    val facturasEmitidas: Int = 0,
    val topClientes: List<Pair<String, Double>> = emptyList(),
    val gastosPorCategoria: List<Pair<String, Double>> = emptyList(),
    val csvParaCompartir: String? = null
)

@HiltViewModel
class InformesViewModel @Inject constructor(
    private val facturaRepository: FacturaRepository,
    private val gastoRepository: GastoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InformesUiState())
    val uiState: StateFlow<InformesUiState> = _uiState.asStateFlow()

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    init {
        setPeriodo(Periodo.MES)
    }

    fun setPeriodo(periodo: Periodo) {
        _uiState.update { it.copy(periodo = periodo) }
        cargarDatos(periodo)
    }

    private fun fechaInicio(periodo: Periodo): Date {
        val cal = Calendar.getInstance()
        return when (periodo) {
            Periodo.MES -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                cal.time
            }
            Periodo.TRIMESTRE -> {
                val month = cal.get(Calendar.MONTH)
                val quarterStart = (month / 3) * 3
                cal.set(Calendar.MONTH, quarterStart)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                cal.time
            }
            Periodo.ANO -> {
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                cal.time
            }
            Periodo.TODO -> Date(0)
        }
    }

    private fun cargarDatos(periodo: Periodo) {
        val desde = fechaInicio(periodo)
        val hasta = Date()

        // Facturado
        viewModelScope.launch {
            facturaRepository.facturacionPeriodo(desde, hasta).collect { facturado ->
                _uiState.update { it.copy(facturado = facturado) }
            }
        }
        // Cobrado
        viewModelScope.launch {
            facturaRepository.cobradoPeriodo(desde, hasta).collect { cobrado ->
                _uiState.update { state ->
                    state.copy(
                        cobrado = cobrado,
                        pendiente = state.facturado - cobrado
                    )
                }
            }
        }
        // IVA
        viewModelScope.launch {
            facturaRepository.ivaPeriodo(desde, hasta).collect { iva ->
                _uiState.update { it.copy(ivaRepercutido = iva) }
            }
        }
        // IRPF
        viewModelScope.launch {
            facturaRepository.irpfPeriodo(desde, hasta).collect { irpf ->
                _uiState.update { state ->
                    state.copy(
                        irpfRetenido = irpf,
                        beneficioNeto = state.facturado - irpf,
                        beneficioReal = state.facturado - irpf - state.gastosPeriodo
                    )
                }
            }
        }
        // Count
        viewModelScope.launch {
            facturaRepository.contarEmitidasPeriodo(desde, hasta).collect { count ->
                _uiState.update { it.copy(facturasEmitidas = count) }
            }
        }
        // Gastos
        viewModelScope.launch {
            gastoRepository.totalPeriodo(desde, hasta).collect { gastos ->
                _uiState.update { state ->
                    state.copy(
                        gastosPeriodo = gastos,
                        beneficioReal = state.beneficioNeto - gastos
                    )
                }
            }
        }
        // Top clientes
        viewModelScope.launch {
            val top = facturaRepository.topClientes(desde, hasta, 5)
            _uiState.update { it.copy(topClientes = top.map { c -> c.clienteNombre to c.total }) }
        }
        // Gastos por categoría
        viewModelScope.launch {
            val categorias = gastoRepository.gastosPorCategoria(desde, hasta)
            _uiState.update { it.copy(gastosPorCategoria = categorias.map { c -> c.categoria to c.total }) }
        }
    }

    fun exportarCSV() {
        val desde = fechaInicio(_uiState.value.periodo)
        val hasta = Date()
        viewModelScope.launch {
            val facturas = facturaRepository.facturasParaExportar(desde, hasta)
            val csv = buildString {
                appendLine("N° Factura;Fecha;Cliente;NIF Cliente;Base Imponible;IVA;IRPF;Total;Estado")
                facturas.forEach { f ->
                    appendLine("${f.numeroFactura};${formatoFecha.format(f.fecha)};${f.clienteNombre};${f.clienteNIF};${f.baseImponible};${f.totalIVA};${f.totalIRPF};${f.totalFactura};${f.estado.descripcion}")
                }
            }
            _uiState.update { it.copy(csvParaCompartir = csv) }
        }
    }

    fun limpiarCSV() {
        _uiState.update { it.copy(csvParaCompartir = null) }
    }
}
