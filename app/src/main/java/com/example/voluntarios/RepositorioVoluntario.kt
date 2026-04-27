package com.example.voluntarios

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.tasks.await


class RepositorioVoluntario(private val voluntarioDao: VoluntarioDao, private val syncManager: SyncManager) {

    private val db = FirebaseFirestore.getInstance()



    suspend fun insertar(voluntario: Voluntario) {


        val voluntarioFirestore = hashMapOf(
            "firebaseUid" to voluntario.firebaseUid,
            "nombre" to voluntario.nombre,
            "apellido" to voluntario.apellido,
            "fechaNac" to voluntario.fechaNac,
            "email" to voluntario.email
        )

        try {
            db.collection("voluntarios")
                .document(voluntario.firebaseUid)
                .set(voluntarioFirestore)
                .await()

        } catch (e: Exception) {
            Log.e("Firestore", "Error guardando voluntario", e)
        }
    }

    suspend fun getByUid(uid: String): Voluntario? {
        return try {
            val doc = db.collection("voluntarios")
                .document(uid)
                .get()
                .await()

            if (!doc.exists()) return null

            Voluntario(
                firebaseUid = uid,
                nombre = doc.getString("nombre") ?: "",
                apellido = doc.getString("apellido") ?: "",
                fechaNac = doc.getString("fechaNac") ?: "",
                email = doc.getString("email") ?: ""
            )
        } catch (e: Exception) {
            Log.e("RepositorioVoluntario", "Error obteniendo voluntario", e)
            null
        }
    }

    suspend fun contar(): Int {
        return try {
            val snapshot = db.collection("voluntarios").get().await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("RepositorioVoluntario", "Error contando voluntarios", e)
            0
        }
    }

    suspend fun actualizar(voluntario: Voluntario) {
        val datos = hashMapOf(
            "nombre" to voluntario.nombre,
            "apellido" to voluntario.apellido,
            "fechaNac" to voluntario.fechaNac,
            "email" to voluntario.email
        )

        try {
            db.collection("voluntarios")
                .document(voluntario.firebaseUid)
                .update(datos as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            Log.e("RepositorioVoluntario", "Error actualizando voluntario", e)
        }
    }

    fun escucharVoluntarios() {

        db.collection("voluntarios")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {

                    val lista = snapshot.documents.map { doc ->
                        Voluntario(
                            firebaseUid = doc.getString("firebaseUid") ?: "",
                            nombre = doc.getString("nombre") ?: "",
                            apellido = doc.getString("apellido") ?: "",
                            fechaNac = doc.getString("fechaNac") ?: "",
                            email = doc.getString("email") ?: ""
                        )
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        val listaFinal = lista.map { nuevo ->

                            val existente = voluntarioDao.getByUid(nuevo.firebaseUid)

                            if (existente != null) {
                                nuevo.copy(id = existente.id)
                            } else {
                                nuevo
                            }
                        }
                        voluntarioDao.insertAll(listaFinal)
                        syncManager.onVoluntariosSynced()
                    }
                }
            }
    }

    suspend fun actualizarVoluntario(voluntario: Voluntario) {

        val voluntarioFirestore = hashMapOf(
            "firebaseUid" to voluntario.firebaseUid,
            "nombre" to voluntario.nombre,
            "apellido" to voluntario.apellido,
            "fechaNac" to voluntario.fechaNac,
            "email" to voluntario.email
        )

        try {
            db.collection("voluntarios")
                .document(voluntario.firebaseUid)
                .set(voluntarioFirestore)
                .await()

        } catch (e: Exception) {
            Log.e("Firestore", "Error actualizando voluntario", e)
        }
    }

}