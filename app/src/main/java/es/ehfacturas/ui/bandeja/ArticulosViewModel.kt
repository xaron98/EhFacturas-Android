package es.ehfacturas.ui.bandeja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.Articulo
import es.ehfacturas.data.db.entity.Categoria
import es.ehfacturas.data.repository.ArticuloRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticulosViewModel @Inject constructor(
    private val articuloRepository: ArticuloRepository
) : ViewModel() {

    val articulos: StateFlow<List<Articulo>> = articuloRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categorias: StateFlow<List<Categoria>> = articuloRepository.obtenerCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _categoriaSeleccionada = MutableStateFlow<Long?>(null)
    val categoriaSeleccionada: StateFlow<Long?> = _categoriaSeleccionada.asStateFlow()

    val articulosFiltrados: StateFlow<List<Articulo>> = combine(
        articulos, _categoriaSeleccionada
    ) { lista, catId ->
        if (catId == null) lista else lista.filter { it.categoriaId == catId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mostrarFormulario = MutableStateFlow(false)
    val mostrarFormulario: StateFlow<Boolean> = _mostrarFormulario.asStateFlow()

    private val _articuloSeleccionado = MutableStateFlow<Articulo?>(null)
    val articuloSeleccionado: StateFlow<Articulo?> = _articuloSeleccionado.asStateFlow()

    fun filtrarPorCategoria(categoriaId: Long?) {
        _categoriaSeleccionada.value = categoriaId
    }

    fun mostrarFormulario(articulo: Articulo? = null) {
        _articuloSeleccionado.value = articulo
        _mostrarFormulario.value = true
    }

    fun cerrarFormulario() {
        _mostrarFormulario.value = false
        _articuloSeleccionado.value = null
    }

    fun guardarArticulo(articulo: Articulo) {
        viewModelScope.launch {
            if (articulo.id == 0L) {
                articuloRepository.guardar(articulo)
            } else {
                articuloRepository.actualizar(articulo.copy(fechaModificacion = java.util.Date()))
            }
            cerrarFormulario()
        }
    }

    fun eliminarArticulo(articulo: Articulo) {
        viewModelScope.launch {
            articuloRepository.actualizar(articulo.copy(activo = false, fechaModificacion = java.util.Date()))
        }
    }
}
