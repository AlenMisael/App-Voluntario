package com.example.voluntarios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RepositorioTurno(
    private val turnoDao: TurnoDao,
    private val context: Context   // ← NUEVO: contexto para notificaciones
) {

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
            "descripcion" to turno.descripcion,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
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
    // NUEVO: escucha SOLO los turnos del voluntario actual
    fun escucharTurnos(uidVoluntario: String) {
        db.collection("turnos")
            .whereEqualTo("uidVoluntario", uidVoluntario)   // ← filtro importante
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                CoroutineScope(Dispatchers.IO).launch {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        Turno(
                            voluntarioId = null,
                            voluntariouid = doc.getString("uidVoluntario") ?: return@mapNotNull null,
                            fireStoreid = doc.id,
                            estado = doc.getString("estado") ?: "pendiente",
                            dia = doc.getString("dia") ?: "",
                            horario = doc.getString("horario") ?: "",
                            direccion = doc.getString("direccion") ?: "",
                            descripcion = doc.getString("descripcion") ?: ""
                        )
                    }
                    // Actualizar Room
                    turnoDao.deleteAll()
                    if (lista.isNotEmpty()) {
                        turnoDao.insertAll(lista)
                    }
                    // Notificar localmente si el turno fue confirmado o cancelado
                    val turnoActual = lista.firstOrNull()
                    turnoActual?.let {
                        when (it.estado) {
                            "confirmado" -> mostrarNotificacionLocal(
                                "Turno confirmado",
                                "Día: ${it.dia} ${it.horario}\nDirección: ${it.direccion}\n${it.descripcion}"
                            )
                            "rechazado" -> mostrarNotificacionLocal(
                                "Turno cancelado",
                                "Motivo: ${it.descripcion}"
                            )
                        }
                    }
                }
            }
    }

    private fun mostrarNotificacionLocal(titulo: String, cuerpo: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "turnos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Turnos",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
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