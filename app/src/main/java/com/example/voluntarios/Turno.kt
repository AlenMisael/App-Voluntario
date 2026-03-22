package com.example.voluntarios

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity (tableName = "turnos", foreignKeys = [ForeignKey(
    entity = Voluntario::class,
    parentColumns = ["id"],
    childColumns = ["id_voluntario"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["id_voluntario"])])
data class Turno (
    @PrimaryKey val id: Int,
    @ColumnInfo(name="id_voluntario") val voluntario:Long,
    @ColumnInfo(name="dia") val dia:String,
    @ColumnInfo(name="horario") val horario:String,
    @ColumnInfo(name="direccion") val direccion:String,
    @ColumnInfo(name="descripcion") val descripcion:String,
    @ColumnInfo(name="estado") val estado:String,

)