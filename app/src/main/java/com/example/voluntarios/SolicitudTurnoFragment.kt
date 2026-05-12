package com.example.voluntarios

import android.adservices.topics.Topic
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SolicitudTurnoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val db = FirebaseFirestore.getInstance()

    private val turnoViewModel: TurnoViewModel by viewModels {
        TurnoViewModel.TurnoViewModelFactory(
            (requireActivity().application as AppVoluntarios).turnoRepositorio,
            (requireActivity().application as AppVoluntarios).voluntarioRepositorio
        )
    }


    private val voluntarioViewModel: VoluntarioViewModel by viewModels {
        VoluntarioViewModel.VoluntarioViewModelFactory((activity?.application as AppVoluntarios).voluntarioRepositorio)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_solicitud_turno, container, false)
    }

    private suspend fun mostrarEstado(
        turno: Turno, voluntario: Voluntario,
        layoutFormulario: View,
        cardEstado: View,
        tvMensaje: TextView,
        tvEstado: TextView
    ) {
        val totalVoluntarios = voluntarioViewModel.contarVoluntarios()
        val card = cardEstado as MaterialCardView

        val colorFondo = when (turno.estado.lowercase()) {
            "pendiente" -> ContextCompat.getColor(requireContext(), R.color.estado_pendiente_bg)
            "confirmado" -> ContextCompat.getColor(requireContext(), R.color.estado_confirmado_bg)
            "rechazado" -> ContextCompat.getColor(requireContext(), R.color.estado_rechazado_bg)
            else -> ContextCompat.getColor(requireContext(), android.R.color.white)
        }
        card.setCardBackgroundColor(colorFondo)

        val colorTexto = ContextCompat.getColor(
            requireContext(),
            R.color.texto_estado
        )

        tvEstado.setTextColor(colorTexto)
        tvMensaje.setTextColor(colorTexto)

        tvEstado.text = when (turno.estado.lowercase()) {
            "pendiente" -> "Estado del turno: Pendiente"
            "confirmado" -> "Estado del turno: Confirmado"
            "rechazado" -> "Estado del turno: Rechazado"
            else -> "Estado: ${turno.estado}"
        }

        tvMensaje.text = when (turno.estado.lowercase()) {
            "confirmado" -> buildString {
                append("¡Felicitades ${voluntario.nombre} ${voluntario.apellido}! ")
                append("Somos $totalVoluntarios voluntarios. ")
                append("Tu turno será el dia ${turno.dia} a las ${turno.horario} horas en ${turno.direccion}. ")

            }

            "rechazado" -> buildString {
                append("Lamentamos informarte que tu turno ha sido cancelado, ${voluntario.nombre}. ")
                append("Motivo: ${turno.descripcion}")
            }

            else -> buildString {
                append("Muchas gracias ${voluntario.nombre} ${voluntario.apellido} ")
                append("por querer participar en el sistema de encuestas. ")
                append("Se te notificará por este medio cuando tu turno haya sido confirmado.")
            }
        }

        layoutFormulario.visibility = View.GONE
        cardEstado.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        auth = FirebaseAuth.getInstance()

        val etNombre = view.findViewById<EditText>(R.id.editTextNombre)
        val etApellido = view.findViewById<EditText>(R.id.editTextApellido)
        val etFecha = view.findViewById<EditText>(R.id.editTextFecha)
        val etTelefono = view.findViewById<EditText>(R.id.editTextTelefono)
        val layoutFormulario = view.findViewById<View>(R.id.layoutFormulario)
        val cardEstado = view.findViewById<View>(R.id.cardEstado)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeExito)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoTurno)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnGuardarTurno = view.findViewById<Button>(R.id.btnGuardarTurno)


        super.onViewCreated(view, savedInstanceState)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        val user = auth.currentUser

        if (user != null) {
            progressBar.visibility = View.VISIBLE
            layoutFormulario.visibility = View.GONE
            cardEstado.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                val voluntario = turnoViewModel.getVoluntarioByUid(user.uid)

                progressBar.visibility = View.GONE

                if (voluntario != null) {

                    turnoViewModel.escucharTurno(user.uid).collect { turno ->

                        progressBar.visibility = View.VISIBLE
                        cardEstado.visibility = View.GONE
                        view.findViewById<View>(R.id.cardResumenEncuesta).visibility = View.GONE
                        layoutFormulario.visibility = View.GONE

                        if (turno != null) {
                            if (turno.estado.lowercase() == "confirmado") {
                                val tieneEncuesta = turnoViewModel.tieneEncuestaCompleta(user.uid)
                                progressBar.visibility = View.GONE
                                if (tieneEncuesta) {
                                    mostrarEncuestaCompleta(
                                        view,
                                        turno,
                                        layoutFormulario,
                                        cardEstado
                                    )
                                } else {
                                    mostrarEstado(
                                        turno,
                                        voluntario,
                                        layoutFormulario,
                                        cardEstado,
                                        tvMensaje,
                                        tvEstado
                                    )
                                }
                            } else {
                                progressBar.visibility = View.GONE
                                mostrarEstado(
                                    turno,
                                    voluntario,
                                    layoutFormulario,
                                    cardEstado,
                                    tvMensaje,
                                    tvEstado
                                )
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            etNombre.setText(voluntario.nombre)
                            etApellido.setText(voluntario.apellido)
                            etFecha.setText(voluntario.fechaNac)
                            etTelefono.setText(voluntario.telefono)
                            layoutFormulario.visibility = View.VISIBLE
                        }
                    }
                }
                else {
                    progressBar.visibility = View.GONE
                    layoutFormulario.visibility = View.VISIBLE
                }

            }
        }


        etFecha.setOnClickListener { mostrarDatePicker(requireContext(), etFecha) }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }
        }

        btnGuardarTurno.setOnClickListener {
            if (user == null) {
                Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            if (nombre.isEmpty() || apellido.isEmpty() || fecha.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Completá todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val voluntario = turnoViewModel.getVoluntarioByUid(user.uid)
                if (voluntario != null) {
                    val voluntarioActualizado = voluntario.copy(
                        nombre = nombre,
                        apellido = apellido,
                        fechaNac = fecha,
                        telefono = telefono
                    )

                    voluntarioViewModel.actualizarVoluntario(voluntarioActualizado)

                    val turno = Turno(
                        voluntarioId = null,
                        voluntariouid = user.uid,
                        estado = "pendiente",
                        asignado = false
                    )
                    turnoViewModel.insertar(turno)

                    mostrarEstado(
                        turno = turno,
                        voluntario = voluntarioActualizado,
                        layoutFormulario = layoutFormulario,
                        cardEstado = cardEstado,
                        tvMensaje = tvMensaje,
                        tvEstado = tvEstado
                    )

                    val topic =
                        TopicHelper.generarTopic(voluntario.firebaseUid, voluntario.nombre)
                    suscribirseANtfy(topic)

                    Toast.makeText(
                        requireContext(),
                        "Turno solicitado (pendiente)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    private fun mostrarEncuestaCompleta(
        view: View,
        turno: Turno,
        layoutFormulario: View,
        cardEstado: View
    ) {
        val cardEncuestaCompleta = view.findViewById<View>(R.id.cardResumenEncuesta)
        val tvFelicitaciones = view.findViewById<TextView>(R.id.tvFelicitacionesEncuesta)
        val tvDiaResumen = view.findViewById<TextView>(R.id.tvResumenDia)
        val tvHorarioResumen = view.findViewById<TextView>(R.id.tvResumenHorario)
        val tvDireccionResumen = view.findViewById<TextView>(R.id.tvResumenDireccion)
        val btnVerResumen = view.findViewById<Button>(R.id.btnVerDetalleEncuesta)

        layoutFormulario.visibility = View.GONE
        cardEstado.visibility = View.GONE
        cardEncuestaCompleta.visibility = View.VISIBLE

        tvDiaResumen.text = "Fecha: ${turno.dia}"
        tvHorarioResumen.text = "Horario: ${turno.horario}"
        tvDireccionResumen.text = "Dirección: ${turno.direccion}"

        btnVerResumen.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ResumenEncuestaFragment())
                .addToBackStack(null)
                .commit()
        }
    }


    private fun suscribirseANtfy(topic: String) {
        Log.d("SolicitudTurno", "Topic: $topic")

        val uri = Uri.parse("https://ntfy.sh/$topic")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.w("SolicitudTurno", "Error abriendo ntfy", e)
            Toast.makeText(requireContext(), "Topic: $topic", Toast.LENGTH_LONG).show()
        }
    }
}
