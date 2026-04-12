package com.example.voluntarios

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.util.Calendar

    fun mostrarDatePicker(context: Context, editText: EditText) {

        val calendario = Calendar.getInstance()

        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(context, { _, y, m, d ->
            val fechaSeleccionada = String.format("%02d/%02d/%d", d, m + 1, y)
            editText.setText(fechaSeleccionada)
        }, year, month, day)

        val calendarioMax = Calendar.getInstance()
        calendarioMax.add(Calendar.YEAR, -18)
        datePicker.datePicker.maxDate = calendarioMax.timeInMillis

        datePicker.show()
    }


