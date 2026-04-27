package com.example.voluntarios

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RepositorioTurno(private val turnoDao: TurnoDao) {

    private val db = FirebaseFirestore.getInstance()
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertar(turno: Turno): String? {

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
            docRef.id
        } catch (e: Exception) {
            Log.e("Firestore", "Error guardando turno", e)
            null
        }
    }


    fun escucharTurnos(onChange: (List<Turno>) -> Unit): ListenerRegistration {
        return db.collection("turnos")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val lista = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("uidVoluntario") ?: return@mapNotNull null
                    Turno(
                        fireStoreid = doc.id,
                        voluntarioId = null,
                        voluntariouid = uid,
                        estado = doc.getString("estado") ?: "pendiente",
                        dia = doc.getString("dia") ?: "",
                        horario = doc.getString("horario") ?: "",
                        direccion = doc.getString("direccion") ?: "",
                        descripcion = doc.getString("descripcion") ?: ""
                    )
                }
                onChange(lista)
            }
    }

    suspend fun getTurnoPorUidVoluntario(uid: String): Turno? {
        return try {
            val snapshot = db.collection("turnos")
                .whereEqualTo("uidVoluntario", uid)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull() ?: return null

            Turno(
                fireStoreid = doc.id,
                voluntarioId = null,
                voluntariouid = uid,
                estado = doc.getString("estado") ?: "pendiente",
                dia = doc.getString("dia") ?: "",
                horario = doc.getString("horario") ?: "",
                direccion = doc.getString("direccion") ?: "",
                descripcion = doc.getString("descripcion") ?: ""
            )
        } catch (e: Exception) {
            Log.e("RepositorioTurno", "Error obteniendo turno", e)
            null
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTurnosUsuario(uid: String): Flow<List<Turno>> {
        return turnoDao.getTurnosDeUsuario(uid)
    }
}