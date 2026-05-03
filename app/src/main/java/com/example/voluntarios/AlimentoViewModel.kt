package com.example.voluntarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AlimentoViewModel(private val repositorio: RepositorioAlimentos): ViewModel() {


    class AlimentoViewModelFactory(private val repositorio: RepositorioAlimentos) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlimentoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AlimentoViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }
}