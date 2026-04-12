package com.example.voluntarios

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VoluntarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(voluntario: Voluntario): Long

    @Query("SELECT * FROM voluntarios WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): Voluntario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(voluntarios: List<Voluntario>)


    @Query("SELECT COUNT(*) FROM voluntarios")
    suspend fun contar(): Int

    @Delete
    suspend fun delete(voluntario: Voluntario)


    @Update
    suspend fun update(voluntario: Voluntario)
}
