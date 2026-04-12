package com.example.voluntarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoluntarioViewModel (private val repositorio: RepositorioVoluntario): ViewModel() {

    private val _voluntario = MutableLiveData<Voluntario?>()
    val voluntario: LiveData<Voluntario?> = _voluntario

    fun insertar(voluntario: Voluntario) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repositorio.insertar(voluntario)

            if (id != -1L) {
                voluntario.id = id
                _voluntario.postValue(voluntario)
            }

        }
    }
    suspend fun getByUid(uid: String): Voluntario? {
        return repositorio.getByUid(uid)
    }



        class VoluntarioViewModelFactory(private val repositorio: RepositorioVoluntario) :
            ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(VoluntarioViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return VoluntarioViewModel(repositorio) as T
                }
                throw IllegalArgumentException("Clase ViewModel desconocida")
            }
        }
    }
