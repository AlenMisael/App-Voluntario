package com.example.voluntarios

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voluntarios")
data class Voluntario(
    @PrimaryKey val id: Int,
    @ColumnInfo(name="nombre") val nombre:String,
    @ColumnInfo(name="apellido") val apellido:String,
    @ColumnInfo(name="fechaNac") val fechaNac:String,
    @ColumnInfo(name="email") val email:String,

)
