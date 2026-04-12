package com.example.voluntarios

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RepositorioTurno(private val turnoDao: TurnoDao) {

    private val db = FirebaseFirestore.getInstance()
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertar(turno: Turno): Long {

        val turnoFirestore = hashMapOf(
            "uidVoluntario" to turno.voluntariouid,
            "estado" to turno.estado,
            "dia" to turno.dia,
            "horario" to turno.horario,
            "direccion" to turno.direccion,
            "descripcion" to turno.descripcion
        )

        return try {
            val docRef = db.collection("turnos")
                .add(turnoFirestore)
                .await()

            val firestoreId = docRef.id

            val turnoConId = turno.copy(
                fireStoreid = firestoreId
            )

            turnoDao.insert(turnoConId)

        } catch (e: Exception) {
            Log.e("Firestore", "Error guardando turno", e)
            -1
        }
    }


    fun escucharTurnos() {

        db.collection("turnos")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val documentos = snapshot.documents

                    CoroutineScope(Dispatchers.IO).launch {



                        val idsFirestore = snapshot.documents.map { it.id }

                        val lista = snapshot.documents.mapNotNull { doc ->

                            val uid = doc.getString("uidVoluntario") ?: return@mapNotNull null


                            Turno(
                                voluntarioId = null,
                                voluntariouid = uid,
                                fireStoreid = doc.id,
                                estado = doc.getString("estado") ?: "pendiente",
                                dia = doc.getString("dia") ?: "",
                                horario = doc.getString("horario") ?: "",
                                direccion = doc.getString("direccion") ?: "",
                                descripcion = doc.getString("descripcion") ?: ""
                            )
                        }
                        if (documentos.isEmpty()) {
                            turnoDao.deleteAll()
                        }
                        else {
                            if (idsFirestore.isNotEmpty()) {
                                turnoDao.deleteNotIn(idsFirestore)
                            }
                            turnoDao.insertAll(lista)
                        }

                    }
                }

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