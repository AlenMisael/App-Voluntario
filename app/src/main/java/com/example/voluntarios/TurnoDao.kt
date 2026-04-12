package com.example.voluntarios

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turno: Turno): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turnos: List<Turno>)

    @Query("DELETE FROM turnos WHERE fireStoreid NOT IN (:idsFirestore)")
    suspend fun deleteNotIn(idsFirestore: List<String>)

    @Query("DELETE FROM turnos")
    suspend fun deleteAll()

    @Query("SELECT * FROM turnos")
    suspend fun getAllOnce(): List<Turno>

    @Update
    suspend fun update(turno: Turno)


    @Query("SELECT * FROM turnos WHERE voluntarioId = :uid ORDER BY id DESC")
    fun getTurnosDeUsuario(uid: String): Flow<List<Turno>>

    @Query("SELECT * FROM turnos WHERE voluntarioId = :id LIMIT 1")
    suspend fun getTurnoPorVoluntario(id: Long): Turno?


}