package com.example.voluntarios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EncuestaViewModel(private val repositorio: RepositorioEncuestas): ViewModel() {

    sealed class EstadoEncuesta {
        object Cargando : EstadoEncuesta()
        data class Completa(
            val encuesta: Encuesta,
            val turno: Turno,
            val alimentos: List<Alimento>
        ) : EstadoEncuesta()
        object Incompleta : EstadoEncuesta()
        object SinEncuesta : EstadoEncuesta()
        data class Error(val mensaje: String) : EstadoEncuesta()
    }


    suspend fun getAlimentosPorTurno(turnoId: String): List<Alimento> {
        return repositorio.getAlimentosPorTurno(turnoId)
    }

    private val _estadoEncuesta = MutableStateFlow<EstadoEncuesta>(EstadoEncuesta.Cargando)
    val estadoEncuesta: StateFlow<EstadoEncuesta> = _estadoEncuesta

    fun verificarEncuestaCompleta(turno: Turno) {
        viewModelScope.launch {
            _estadoEncuesta.value = EstadoEncuesta.Cargando
            try {
                val turnoId = turno.fireStoreid
                    ?: run {
                        _estadoEncuesta.value = EstadoEncuesta.SinEncuesta
                        return@launch
                    }

                val db = FirebaseFirestore.getInstance()

                // collectionGroup busca en usuarios/{cualquierUid}/encuestas
                // filtrando por el turnoId que tiene la encuesta
                val encuestaSnapshot = db
                    .collectionGroup("encuestas")
                    .whereEqualTo("turnoId", turnoId)
                    .whereEqualTo("completa", true)
                    .get()
                    .await()

                if (encuestaSnapshot.isEmpty) {
                    _estadoEncuesta.value = EstadoEncuesta.Incompleta
                    return@launch
                }

                val encuestaDoc = encuestaSnapshot.documents.first()
                val encuesta = encuestaDoc.toObject(Encuesta::class.java)
                    ?: run {
                        _estadoEncuesta.value = EstadoEncuesta.Error("No se pudo leer la encuesta")
                        return@launch
                    }

                val alimentosSnapshot = encuestaDoc.reference
                    .collection("alimentos")
                    .get()
                    .await()

                val alimentos = alimentosSnapshot.documents.mapNotNull {
                    it.toObject(Alimento::class.java)
                }

                _estadoEncuesta.value = EstadoEncuesta.Completa(
                    encuesta = encuesta,
                    turno = turno,
                    alimentos = alimentos
                )

            } catch (e: Exception) {
                Log.e("EncuestaViewModel", "Error verificando encuesta", e)
                _estadoEncuesta.value = EstadoEncuesta.Error(e.message ?: "Error desconocido")
            }
        }
    }

    class EncuestaViewModelFactory(
        private val repositorio: RepositorioEncuestas
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EncuestaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EncuestaViewModel(repositorio) as T
            }
            throw IllegalArgumentException("ViewModel desconocido")
        }
    }

}
