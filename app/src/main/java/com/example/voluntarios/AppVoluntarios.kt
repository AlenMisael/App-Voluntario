package com.example.voluntarios

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppVoluntarios: Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val voluntarioRepositorio by lazy {
        RepositorioVoluntario(database.voluntarioDao())
    }

    val turnoRepositorio by lazy {
        RepositorioTurno(database.turnoDao())
    }
}


