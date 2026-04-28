package com.example.voluntarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class TurnoViewModel(
    private val turnoRepositorio: RepositorioTurno,
    private val voluntarioRepositorio: RepositorioVoluntario
) : ViewModel() {

    private val _turnoActual = MutableStateFlow<Turno?>(null)
    val turnoActual: StateFlow<Turno?> = _turnoActual.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening() {
        listenerRegistration = turnoRepositorio.escucharTurnos { lista ->
            _turnos.postValue(lista)
        }
    }

    init {
        startListening()
    }


    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    suspend fun insertar(turno: Turno): String? {
        return turnoRepositorio.insertar(turno)
    }

    suspend fun getTurnoPorUidVoluntario(uid: String): Turno? {
        return turnoRepositorio.getTurnoPorUidVoluntario(uid)
    }

    suspend fun getVoluntarioByUid(uid: String): Voluntario? {
        return voluntarioRepositorio.getByUid(uid)
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
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
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}