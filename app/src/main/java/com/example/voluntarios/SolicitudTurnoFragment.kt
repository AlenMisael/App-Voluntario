package com.example.voluntarios

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import java.util.Calendar

class SolicitudTurnoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private val turnoViewModel: TurnoViewModel by viewModels {
        TurnoViewModel.TurnoViewModelFactory(
            (requireActivity().application as AppVoluntarios).turnoRepositorio,
            (requireActivity().application as AppVoluntarios).voluntarioRepositorio
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_solicitud_turno, container, false)
    }

    private suspend fun mostrarEstado(
        turno: Turno,
        layoutFormulario: View,
        cardEstado: View,
        tvMensaje: TextView,
        tvEstado: TextView
    ) {
        val totalVoluntarios = turnoViewModel.contarVoluntarios()

        val card = cardEstado as MaterialCardView

        val colorFondo = when (turno.estado.lowercase()) {
            "pendiente" -> ContextCompat.getColor(requireContext(), R.color.estado_pendiente_bg)
            "confirmado" -> ContextCompat.getColor(requireContext(), R.color.estado_confirmado_bg)
            "rechazado" -> ContextCompat.getColor(requireContext(), R.color.estado_rechazado_bg)
            else -> ContextCompat.getColor(requireContext(), android.R.color.white)
        }

        card.setCardBackgroundColor(colorFondo)


        tvEstado.text = "Tu turno se encuentra en estado: ${turno.estado}"

        tvEstado.text = when (turno.estado.lowercase()) {
            "pendiente" -> "Estado del turno: Pendiente"
            "confirmado" -> "Estado del turno: Confirmado"
            "rechazado" -> "Estado del turno: Rechazado"
            else -> "Estado: ${turno.estado}"
        }

        tvMensaje.text = "Muchas gracias por querer participar en el sistema de encuestas. " +
                "Somos $totalVoluntarios voluntarios en total. " +
                "Se te notificará por este medio cuando tu turno haya sido confirmado."

        layoutFormulario.visibility = View.GONE
        cardEstado.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val etNombre = view.findViewById<EditText>(R.id.editTextNombre)
        val etApellido = view.findViewById<EditText>(R.id.editTextApellido)
        val etFecha = view.findViewById<EditText>(R.id.editTextFecha)

        val layoutFormulario = view.findViewById<View>(R.id.layoutFormulario)
        val cardEstado = view.findViewById<View>(R.id.cardEstado)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeExito)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoTurno)

        super.onViewCreated(view, savedInstanceState)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        if (user != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val voluntario = turnoViewModel.getVoluntarioByUid(user.uid)

                if (voluntario != null) {

                    val turno = turnoViewModel.getTurnoPorVoluntario(voluntario.id)

                    if (turno != null) {
                        mostrarEstado(turno,layoutFormulario, cardEstado, tvMensaje, tvEstado)
                    }
                    else {
                        voluntario?.let {
                            etNombre.setText(it.nombre)
                            etApellido.setText(it.apellido)
                            etFecha.setText(it.fechaNac)
                        }
                    }
                }

            }
        }



        etFecha.setOnClickListener {
            mostrarDatePicker(requireContext(),etFecha)
        }

        val btnGuardarTurno = view.findViewById<Button>(R.id.btnGuardarTurno)

        btnCerrarSesion.setOnClickListener {
            auth.signOut()

            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }

        btnGuardarTurno.setOnClickListener {
            if (user == null) {
                Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val nombre = etNombre.text.toString()
            val apellido = etApellido.text.toString()
            val fecha = etFecha.text.toString()

            if (nombre.isEmpty() || apellido.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            viewLifecycleOwner.lifecycleScope.launch {
                val voluntario = turnoViewModel.getVoluntarioByUid(user.uid)

                if (voluntario != null) {

                    val voluntarioActualizado = voluntario.copy(
                        nombre = nombre,
                        apellido = apellido,
                        fechaNac = fecha
                    )

                    turnoViewModel.actualizarVoluntario(voluntarioActualizado)

                    val turno = Turno(
                        voluntarioId = voluntario.id,
                        estado = "pendiente"
                    )

                    turnoViewModel.insertar(turno)
                    mostrarEstado(turno, layoutFormulario, cardEstado, tvMensaje, tvEstado)

                    Toast.makeText(
                        requireContext(),
                        "Turno creado en estado pendiente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
