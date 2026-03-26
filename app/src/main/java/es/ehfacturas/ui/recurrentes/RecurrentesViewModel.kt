package es.ehfacturas.ui.recurrentes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.dao.FacturaRecurrenteDao
import es.ehfacturas.data.db.entity.FacturaRecurrente
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurrentesViewModel @Inject constructor(
    private val facturaRecurrenteDao: FacturaRecurrenteDao
) : ViewModel() {

    val recurrentes: StateFlow<List<FacturaRecurrente>> = facturaRecurrenteDao.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleActivo(recurrente: FacturaRecurrente) {
        viewModelScope.launch {
            facturaRecurrenteDao.actualizar(recurrente.copy(activo = !recurrente.activo))
        }
    }

    fun eliminar(recurrente: FacturaRecurrente) {
        viewModelScope.launch {
            facturaRecurrenteDao.eliminar(recurrente)
        }
    }
}
