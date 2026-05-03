package com.example.voluntarios

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity (tableName = "alimentos", foreignKeys = [ForeignKey(
    entity = Encuesta::class,
    parentColumns = ["encuestaId"],
    childColumns = ["encuesta"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["encuesta"])])

data class Alimento(
    @PrimaryKey( autoGenerate = true)
    var alimentoid: Int = 0,

    @ColumnInfo (name = "encuesta")
    var encuestaId: Int,

    @ColumnInfo(name = "alimento")
    var nombre_alimento: String,

    @ColumnInfo(name = "categoria")
    var categoria: String,

    @ColumnInfo(name = "cantidad")
    var cantidad_alimento: String,

    @ColumnInfo(name = "numero_veces")
    var numero_veces: String,

    @ColumnInfo(name = "frecuencia")
    var frecuencia_veces: String,

    @ColumnInfo(name = "gramos")
    var gramos: Float,

    @ColumnInfo(name = "kcal_totales")
    var kcal: Float,

    @ColumnInfo(name = "carbohidratos")
    var carbohidratos: Float,

    @ColumnInfo(name = "proteinas")
    var proteinas: Float,

    @ColumnInfo(name = "grasas_totales")
    var grasas: Float,

    @ColumnInfo(name = "alcohol")
    var alcohol: Float,

    @ColumnInfo(name = "colesterol")
    var colesterol: Float,

    @ColumnInfo(name ="fibra")
    var fibra: Float

)