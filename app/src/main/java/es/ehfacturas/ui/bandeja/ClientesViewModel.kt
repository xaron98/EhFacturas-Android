package es.ehfacturas.ui.bandeja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.Cliente
import es.ehfacturas.data.repository.ClienteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    val clientes: StateFlow<List<Cliente>> = clienteRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contarActivos: StateFlow<Int> = clienteRepository.contarActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _resultadosBusqueda = MutableStateFlow<List<Cliente>>(emptyList())
    val resultadosBusqueda: StateFlow<List<Cliente>> = _resultadosBusqueda.asStateFlow()

    private val _clienteSeleccionado = MutableStateFlow<Cliente?>(null)
    val clienteSeleccionado: StateFlow<Cliente?> = _clienteSeleccionado.asStateFlow()

    private val _mostrarFormulario = MutableStateFlow(false)
    val mostrarFormulario: StateFlow<Boolean> = _mostrarFormulario.asStateFlow()

    fun buscar(texto: String) {
        _busqueda.value = texto
        if (texto.length >= 2) {
            viewModelScope.launch {
                _resultadosBusqueda.value = clienteRepository.buscar(texto)
            }
        } else {
            _resultadosBusqueda.value = emptyList()
        }
    }

    fun seleccionarCliente(cliente: Cliente?) {
        _clienteSeleccionado.value = cliente
    }

    fun mostrarFormulario(cliente: Cliente? = null) {
        _clienteSeleccionado.value = cliente
        _mostrarFormulario.value = true
    }

    fun cerrarFormulario() {
        _mostrarFormulario.value = false
        _clienteSeleccionado.value = null
    }

    fun guardarCliente(cliente: Cliente) {
        viewModelScope.launch {
            if (cliente.id == 0L) {
                clienteRepository.guardar(cliente)
            } else {
                clienteRepository.actualizar(cliente.copy(fechaModificacion = java.util.Date()))
            }
            cerrarFormulario()
        }
    }

    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            clienteRepository.actualizar(cliente.copy(activo = false, fechaModificacion = java.util.Date()))
        }
    }
}
