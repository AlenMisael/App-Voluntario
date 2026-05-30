package com.example.voluntarios

import android.adservices.topics.Topic
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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
import android.text.TextWatcher
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
        tvEstado: TextView,
        tvSolicitudOtroTurno: TextView,
        etNombre: EditText,
        etApellido: EditText,
        etFecha: EditText,
        etTelefono: EditText
    ) {
        val totalVoluntarios = voluntarioViewModel.contarVoluntarios()
        val card = cardEstado as MaterialCardView

        val colorFondo = when (turno.estado.lowercase()) {
            "pendiente" -> ContextCompat.getColor(requireContext(), R.color.estado_pendiente_bg)
            "confirmado" -> ContextCompat.getColor(requireContext(), R.color.estado_confirmado_bg)
            "cancelado" -> ContextCompat.getColor(requireContext(), R.color.estado_cancelado_bg)
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
            "cancelado" -> "Estado del turno: Cancelado"
            else -> "Estado: ${turno.estado}"
        }

        tvMensaje.text = when (turno.estado.lowercase()) {
            "confirmado" -> buildString {
                append("¡Felicitades ${voluntario.nombre} ${voluntario.apellido}! ")
                append("Somos $totalVoluntarios voluntarios. ")
                append("Tu turno será el dia ${turno.dia} a las ${turno.horario} horas en ${turno.direccion}. ")

            }

            "cancelado" -> buildString {
                append("Lamentamos informarte que tu turno ha sido cancelado, ${voluntario.nombre}. ")
                append("Motivo: ${turno.descripcion}")
            }

            else -> buildString {
                append("Muchas gracias ${voluntario.nombre} ${voluntario.apellido} ")
                append("por querer participar en el sistema de encuestas. ")
                append("Se te notificará por este medio cuando tu turno haya sido confirmado.")
            }
        }

        if (turno.estado.lowercase() == "cancelado") {
            tvSolicitudOtroTurno.visibility = View.VISIBLE
            tvSolicitudOtroTurno.setOnClickListener {
                etNombre.setText(voluntario.nombre)
                etApellido.setText(voluntario.apellido)
                etFecha.setText(voluntario.fechaNac)
                etTelefono.setText(voluntario.telefono)

                cardEstado.visibility = View.GONE
                layoutFormulario.visibility = View.VISIBLE
            }
        } else {
            tvSolicitudOtroTurno.visibility = View.GONE
        }

        layoutFormulario.visibility = View.GONE
        cardEstado.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSolicitudOtroTurno = view.findViewById<TextView>(R.id.tvSolicitudOtroTurno)

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


        val filtroSoloLetras = InputFilter { source, _, _, _, _, _ ->
            if (source.all { it.isLetter() || it.isWhitespace() }) null else ""
        }
        etNombre.filters = arrayOf(filtroSoloLetras)
        etApellido.filters = arrayOf(filtroSoloLetras)

        etTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val telefono = s.toString().trim()
                if (telefono.isNotEmpty() && (telefono.length < 10 || telefono.length > 13)) {
                    etTelefono.error = "El teléfono debe tener entre 10 y 13 números"
                } else {
                    etTelefono.error = null
                }
            }
        })

        savedInstanceState?.let { bundle ->
            etNombre.setText(bundle.getString("nombre", ""))
            etApellido.setText(bundle.getString("apellido", ""))
            etFecha.setText(bundle.getString("fechaNac", ""))
            etTelefono.setText(bundle.getString("telefono", ""))
        }


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
                                        cardEstado,
                                    )
                                } else {
                                    mostrarEstado(
                                        turno,
                                        voluntario,
                                        layoutFormulario,
                                        cardEstado,
                                        tvMensaje,
                                        tvEstado,
                                        tvSolicitudOtroTurno,
                                        etNombre,
                                        etApellido,
                                        etFecha,
                                        etTelefono
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
                                    tvEstado,
                                    tvSolicitudOtroTurno,
                                    etNombre,
                                    etApellido,
                                    etFecha,
                                    etTelefono
                                )
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            if (savedInstanceState == null) {
                                etNombre.setText(voluntario.nombre)
                                etApellido.setText(voluntario.apellido)
                                etFecha.setText(voluntario.fechaNac)
                                etTelefono.setText(voluntario.telefono)
                            }
                        }
                    }
                }
                else {
                    progressBar.visibility = View.GONE
                    val nombreCompleto = user.displayName?.trim()?.split(" ")?.filter { it.isNotBlank() }
                    etNombre.setText(nombreCompleto?.firstOrNull() ?: "")
                    etApellido.setText(if ((nombreCompleto?.size ?: 0) > 1) nombreCompleto?.drop(1)?.joinToString(" ") else "")
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
            if (etTelefono.error != null) {
                etTelefono.requestFocus()
                return@setOnClickListener
            }

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
                var voluntario = turnoViewModel.getVoluntarioByUid(user.uid)
                if (voluntario == null) {
                    val nuevoVoluntario = Voluntario(
                        firebaseUid = user.uid,
                        nombre = nombre,
                        apellido = apellido,
                        fechaNac = fecha,
                        telefono = telefono,
                        email = user.email ?: ""
                    )
                    voluntarioViewModel.insertar(nuevoVoluntario)
                    voluntario = turnoViewModel.getVoluntarioByUid(user.uid)
                }

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
                        tvEstado = tvEstado,
                        tvSolicitudOtroTurno = tvSolicitudOtroTurno,
                        etNombre = etNombre,
                        etApellido = etApellido,
                        etFecha = etFecha,
                        etTelefono = etTelefono
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        view?.let {
            val layoutFormulario = it.findViewById<View>(R.id.layoutFormulario)
            if (layoutFormulario.visibility == View.VISIBLE) {
                outState.putString("nombre",   it.findViewById<EditText>(R.id.editTextNombre).text.toString())
                outState.putString("apellido", it.findViewById<EditText>(R.id.editTextApellido).text.toString())
                outState.putString("fechaNac", it.findViewById<EditText>(R.id.editTextFecha).text.toString())
                outState.putString("telefono", it.findViewById<EditText>(R.id.editTextTelefono).text.toString())
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
            val fragment = ResumenEncuestaFragment().apply {
                arguments = Bundle().apply {
                    putString("turnoId", turno.fireStoreid)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }


    private fun suscribirseANtfy(topic: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("ntfy://ntfy.sh/$topic?auto-subscribe=1"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            Log.d("SolicitudTurno", "Auto-suscrito a tópico: $topic")
        } catch (e: Exception) {
            Log.w("SolicitudTurno", "App ntfy no instalada", e)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ntfy.sh/app")))
        }
    }
}
