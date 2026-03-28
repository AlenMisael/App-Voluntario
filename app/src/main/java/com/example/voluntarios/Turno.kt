package com.example.voluntarios

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "turnos",
    foreignKeys = [
        ForeignKey(
            entity = Voluntario::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["firebaseUid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["firebaseUid"])]
)
data class Turno(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firebaseUid: String,
    val dia: String,
    val horario: String,
    val direccion: String,
    val descripcion: String,
    val estado: String = "pendiente"
)