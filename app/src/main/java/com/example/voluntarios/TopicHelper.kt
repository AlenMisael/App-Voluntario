package com.example.voluntarios

import java.util.*

object TopicHelper {

    fun generarTopic(uid: String, nombre: String): String {
        val nombreLimpio = nombre
            .uppercase(Locale.getDefault())
            .replace(Regex("[^A-Z0-9]"), "")
        val sufijo = generarSufijo(uid)
        return "TURNO_${nombreLimpio}_$sufijo"
    }

    private fun generarSufijo(uid: String): String {
        val lastTwo = uid.takeLast(2)
        val primerasDosLetras = uid.filter { it.isLetter() }.take(2).toString()
        val primerosDosNumeros = uid.filter { it.isDigit() }.take(2).toString()

        return if (lastTwo.all { it.isDigit() }) {
            val letras = if (primerasDosLetras.length >= 2) primerasDosLetras else "XX"
            letras.take(2) + lastTwo
        } else if (lastTwo.all { it.isLetter() }) {
            val numeros = if (primerosDosNumeros.length >= 2) primerosDosNumeros else "00"
            numeros.take(2) + lastTwo.uppercase()
        } else {
            val letras = uid.filter { it.isLetter() }.take(2).toString()
            val digitos = uid.filter { it.isDigit() }.takeLast(2).toString()
            (letras + digitos).take(4).padEnd(4, 'X')
        }
    }
}