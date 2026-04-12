package com.example.voluntarios

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RepositorioTurno(private val turnoDao: TurnoDao) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertar(turno: Turno): Long {
        return turnoDao.insert(turno)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTurnoPorVoluntario(voluntarioId: Long): Turno? {
        return turnoDao.getTurnoPorVoluntario(voluntarioId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTurnosUsuario(uid: String): Flow<List<Turno>> {
        return turnoDao.getTurnosDeUsuario(uid)
    }
}