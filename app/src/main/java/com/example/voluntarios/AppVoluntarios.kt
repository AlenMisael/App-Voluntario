package com.example.voluntarios

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppVoluntarios : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var syncManager: SyncManager

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val voluntarioRepositorio by lazy {
        RepositorioVoluntario(database.voluntarioDao(), syncManager)
    }

    val turnoRepositorio by lazy {
        RepositorioTurno(database.turnoDao(), applicationContext)  // ← se pasa el contexto
    }

    override fun onCreate() {
        super.onCreate()

        syncManager = SyncManager(
            voluntarioDao = database.voluntarioDao(),
            turnoDao = database.turnoDao(),
            scope = applicationScope
        )

    }

    private fun iniciarSincronizacion() {
        val voluntarioRepo = RepositorioVoluntario(database.voluntarioDao(), syncManager)
        voluntarioRepo.escucharVoluntarios()

        // Iniciar escucha de turnos solo si hay usuario logueado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            turnoRepositorio.escucharTurnos(currentUser.uid)
        }
    }
}