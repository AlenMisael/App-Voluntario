package com.example.voluntarios

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turno: Turno): Long

    @Query("SELECT * FROM turnos WHERE voluntarioId = :uid ORDER BY id DESC")
    fun getTurnosDeUsuario(uid: String): Flow<List<Turno>>

    @Query("SELECT * FROM turnos WHERE voluntarioId = :id LIMIT 1")
    suspend fun getTurnoPorVoluntario(id: Long): Turno?


}