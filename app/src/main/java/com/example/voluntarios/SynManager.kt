package com.example.voluntarios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(
    private val voluntarioDao: VoluntarioDao,
    private val turnoDao: TurnoDao,
    private val scope: CoroutineScope

) {

    private var voluntariosCargados = false

    fun onVoluntariosSynced() {
        voluntariosCargados = true
        resolverTurnosSinId()
    }

    fun resolverTurnosSinId() {

        scope.launch(Dispatchers.IO) {

            val turnos = turnoDao.getAllOnce()

            turnos.forEach { turno ->

                if (turno.voluntarioId == null) {

                    val voluntario = voluntarioDao.getByUid(turno.voluntariouid)

                    if (voluntario != null) {

                        turnoDao.update(
                            turno.copy(voluntarioId = voluntario.id)
                        )
                    }
                }
            }
        }
    }
}