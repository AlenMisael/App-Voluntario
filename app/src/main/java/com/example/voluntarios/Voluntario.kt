package com.example.voluntarios

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voluntarios")
data class Voluntario(
    @PrimaryKey val firebaseUid: String,
    val nombre: String,
    val apellido: String,
    val fechaNac: String,
    val email: String
)