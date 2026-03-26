package es.ehfacturas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.Negocio
import es.ehfacturas.data.preferences.AppPreferences
import es.ehfacturas.data.repository.ArticuloRepository
import es.ehfacturas.data.repository.ClienteRepository
import es.ehfacturas.data.repository.GastoRepository
import es.ehfacturas.data.repository.NegocioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val negocioRepository: NegocioRepository,
    private val clienteRepository: ClienteRepository,
    private val articuloRepository: ArticuloRepository,
    private val gastoRepository: GastoRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val negocio: StateFlow<Negocio?> = negocioRepository.obtenerNegocio()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val temaApp: StateFlow<String> = appPreferences.temaApp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    val cloudProvider: StateFlow<String> = appPreferences.cloudProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "claude")

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun guardarNegocio(negocio: Negocio) {
        viewModelScope.launch {
            if (negocio.id == 0L) {
                negocioRepository.guardar(negocio)
            } else {
                negocioRepository.actualizar(negocio)
            }
            _mensaje.value = "Guardado"
        }
    }

    fun setTema(tema: String) {
        viewModelScope.launch { appPreferences.setTemaApp(tema) }
    }

    fun setCloudProvider(provider: String) {
        viewModelScope.launch { appPreferences.setCloudProvider(provider) }
    }

    fun exportarDatos(): String {
        // Se ejecutará en coroutine desde la UI
        return "{}" // Placeholder — se implementa en exportarDatosAsync
    }

    fun exportarDatosAsync(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val clientes = clienteRepository.obtenerTodosSync()
            val articulos = articuloRepository.obtenerTodosSync()
            val gastos = gastoRepository.obtenerTodosSync()

            val json = JSONObject().apply {
                put("clientes", JSONArray().apply {
                    clientes.forEach { c ->
                        put(JSONObject().apply {
                            put("nombre", c.nombre)
                            put("nif", c.nif)
                            put("email", c.email)
                            put("telefono", c.telefono)
                            put("direccion", c.direccion)
                            put("ciudad", c.ciudad)
                            put("provincia", c.provincia)
                        })
                    }
                })
                put("articulos", JSONArray().apply {
                    articulos.forEach { a ->
                        put(JSONObject().apply {
                            put("nombre", a.nombre)
                            put("referencia", a.referencia)
                            put("precioUnitario", a.precioUnitario)
                            put("unidad", a.unidad.abreviatura)
                        })
                    }
                })
                put("gastos", JSONArray().apply {
                    gastos.forEach { g ->
                        put(JSONObject().apply {
                            put("concepto", g.concepto)
                            put("importe", g.importe)
                            put("categoria", g.categoria)
                        })
                    }
                })
            }
            onResult(json.toString(2))
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}
