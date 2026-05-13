package com.example.voluntarios

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioEncuestas {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getAlimentosPorTurno(turnoId: String): List<Alimento> {
        return try {

            val encuestaSnapshot = try {
                db.collection("encuestas")
                    .whereEqualTo("turnoId", turnoId)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.e("FIREBASE", "FALLO en lectura de encuestas: ${e.message}")
                return emptyList()
            }
            if (encuestaSnapshot.isEmpty) return emptyList()

            val encuestaDoc = encuestaSnapshot.documents.first()

            val alimentosSnapshot = try {
                db.collection("encuestas")
                    .document(encuestaDoc.id)
                    .collection("alimentos")
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.e("FIREBASE", "FALLO en lectura de alimentos: ${e.message}")
                return emptyList()
            }

            alimentosSnapshot.documents.mapNotNull { doc ->
                if (doc.exists()) {

                    Alimento(
                        nombre_alimento = doc.id,
                        numero_veces = doc.getString("numero_veces") ?: "",
                        cantidad_alimento = doc.getString("cantidad") ?: "",
                        frecuencia_veces = doc.getString("frecuencia") ?: "",
                        gramos = doc.getDouble("gramos")?.toFloat() ?: 0f,
                        kcal = doc.getDouble("kcal")?.toFloat() ?: 0f,
                        carbohidratos = doc.getDouble("carbohidratos")?.toFloat() ?: 0f,
                        proteinas = doc.getDouble("proteinas")?.toFloat() ?: 0f,
                        grasas = doc.getDouble("grasas")?.toFloat() ?: 0f,
                        alcohol = doc.getDouble("alcohol")?.toFloat() ?: 0f,
                        colesterol = doc.getDouble("colesterol")?.toFloat() ?: 0f,
                        fibra = doc.getDouble("fibra")?.toFloat() ?: 0f,
                        categoria = doc.getString("categoria") ?: "",
                        encuestaId = 0
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error obteniendo alimentos: ${e.message}")
            emptyList()
        }
    }
}