package com.example.voluntarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TurnoViewModel(
    private val repositorio: RepositorioTurno,
    private val voluntarioRepositorio: RepositorioVoluntario
) : ViewModel() {

    private val _turnoActual = MutableStateFlow<Turno?>(null)
    val turnoActual: StateFlow<Turno?> = _turnoActual.asStateFlow()

    fun cargarTurno(voluntarioId: Long) {
        viewModelScope.launch {
            val turno = repositorio.getTurnoPorVoluntario(voluntarioId)
            _turnoActual.value = turno
        }
    }

    suspend fun getVoluntarioByUid(uid: String): Voluntario? = voluntarioRepositorio.getByUid(uid)
    suspend fun contarVoluntarios(): Int = voluntarioRepositorio.contar()
    suspend fun actualizarVoluntario(voluntario: Voluntario) = voluntarioRepositorio.actualizarVoluntario(voluntario)

    fun insertar(turno: Turno) {
        viewModelScope.launch {
            repositorio.insertar(turno)
        }
    }

    suspend fun getTurnoPorVoluntario(voluntarioId: Long): Turno? {
        return repositorio.getTurnoPorVoluntario(voluntarioId)
    }

    class TurnoViewModelFactory(
        private val turnoRepositorio: RepositorioTurno,
        private val voluntarioRepositorio: RepositorioVoluntario
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TurnoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TurnoViewModel(turnoRepositorio, voluntarioRepositorio) as T
            }
            throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}