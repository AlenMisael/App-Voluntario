package com.example.voluntarios

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VoluntarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(voluntario: Voluntario): Long

    @Query("SELECT * FROM voluntarios WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): Voluntario?
}