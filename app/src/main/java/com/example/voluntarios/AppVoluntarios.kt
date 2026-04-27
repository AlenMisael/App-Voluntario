package com.example.voluntarios

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppVoluntarios: Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var syncManager: SyncManager

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val voluntarioRepositorio by lazy {
        RepositorioVoluntario(database.voluntarioDao(), syncManager)
    }

    val turnoRepositorio by lazy {
        RepositorioTurno(database.turnoDao())
    }

    override fun onCreate() {
        super.onCreate()

        val db = AppDatabase.getInstance(this)

        syncManager = SyncManager(
            voluntarioDao = db.voluntarioDao(),
            turnoDao = db.turnoDao(),
            scope = applicationScope
        )

    }

}


