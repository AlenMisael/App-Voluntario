package com.example.voluntarios

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "voluntarios",
    indices = [Index(value = ["firebaseUid"], unique = true)]
)
data class Voluntario(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val firebaseUid: String,
    val nombre: String,
    val apellido: String,
    val fechaNac: String,
    val email: String
)