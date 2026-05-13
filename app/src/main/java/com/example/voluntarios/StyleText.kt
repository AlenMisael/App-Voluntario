package com.example.voluntarios

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView

fun TextView.setStatText(label: String, valor: String, unidad: String = "") {
    val texto = SpannableString("$label\n$valor $unidad".trim())

    texto.setSpan(ForegroundColorSpan(Color.GRAY), 0, label.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    texto.setSpan(RelativeSizeSpan(0.85f), 0, label.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    texto.setSpan(StyleSpan(Typeface.BOLD), label.length + 1, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    texto.setSpan(RelativeSizeSpan(1.3f), label.length + 1, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    this.text = texto
}