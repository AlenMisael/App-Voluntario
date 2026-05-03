package com.example.voluntarios

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "encuestas")
data class Encuesta(
    @PrimaryKey(autoGenerate = true)
    var encuestaId: Int = 0,

    @ColumnInfo(name = "firestore_id")
    var firestoreId: String? = null,

    @ColumnInfo(name = "completada")
    var completa: Boolean = false,

    @ColumnInfo(name = "domicilio")
    var domicilio: String = "",

    @ColumnInfo(name = "ciudad")
    var ciudad: String = "",

    @ColumnInfo(name= "longitud")
    var lon: Double = 0.0,

    @ColumnInfo(name = "latitud")
    var lan: Double = 0.0,

    @ColumnInfo(name = "user_uid")
    var userUid: String = "",

    @ColumnInfo(name = "current_index")
    var currentIndex: Int = 0,

    @ColumnInfo(name = "activa")
    var activa: Boolean = true,

    @ColumnInfo(name = "updated_at")
    var updatedAt: Long? = null,

    @ColumnInfo(name = "turnoId")
    var turnoId: String? = null
)