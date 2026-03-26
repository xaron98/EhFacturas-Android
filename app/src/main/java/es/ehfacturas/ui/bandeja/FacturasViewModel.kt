package es.ehfacturas.ui.bandeja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.EstadoFactura
import es.ehfacturas.data.db.entity.Factura
import es.ehfacturas.data.repository.FacturaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FacturasViewModel @Inject constructor(
    private val facturaRepository: FacturaRepository
) : ViewModel() {

    val facturas: StateFlow<List<Factura>> = facturaRepository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filtroEstado = MutableStateFlow<EstadoFactura?>(null)
    val filtroEstado: StateFlow<EstadoFactura?> = _filtroEstado.asStateFlow()

    val facturasFiltradas: StateFlow<List<Factura>> = combine(
        facturas, _filtroEstado
    ) { lista, estado ->
        if (estado == null) lista else lista.filter { it.estado == estado }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats del mes actual
    private val inicioMes: Date
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

    private val finMes: Date
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            return cal.time
        }

    val facturacionMes: StateFlow<Double> = facturaRepository
        .facturacionPeriodo(inicioMes, finMes)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalFacturas: StateFlow<Int> = facturaRepository.contarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendientes: StateFlow<Int> = facturaRepository.contarPorEstado(EstadoFactura.EMITIDA)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun filtrarPorEstado(estado: EstadoFactura?) {
        _filtroEstado.value = estado
    }

    fun eliminarFactura(factura: Factura) {
        viewModelScope.launch {
            facturaRepository.eliminar(factura)
        }
    }
}
