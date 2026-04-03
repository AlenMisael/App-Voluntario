package com.example.voluntarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TurnoViewModel(private val repositorio: RepositorioTurno,private val voluntarioRepositorio: RepositorioVoluntario
): ViewModel() {

    private val _turnos = MutableLiveData<List<Turno>>()
    val turnos: LiveData<List<Turno>> = _turnos

    fun insertar(turno: Turno) {
        viewModelScope.launch {
            val id = repositorio.insertar(turno)
            turno.id = id
        }
    }

    suspend fun getVoluntarioByUid(uid: String): Voluntario? {
        return voluntarioRepositorio.getByUid(uid)
    }

    fun getTurnosUsuario(uid: String) {
        viewModelScope.launch {
            repositorio.getTurnosUsuario(uid).collect { turnos ->
                _turnos.value = turnos
            }
        }
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
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }

}