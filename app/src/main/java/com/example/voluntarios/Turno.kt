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
            parentColumns = ["id"],
            childColumns = ["voluntarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["fireStoreid"], unique = true)]
)
data class Turno(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val voluntarioId: Long?,
    val voluntariouid: String = "",
    val fireStoreid: String = "",
    val dia: String = "",
    val horario: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val estado: String = "pendiente"
)