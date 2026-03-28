package com.example.voluntarios

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turno: Turno)

    @Query("SELECT * FROM turnos WHERE firebaseUid = :uid ORDER BY id DESC")
    fun getTurnosDeUsuario(uid: String): Flow<List<Turno>>
}