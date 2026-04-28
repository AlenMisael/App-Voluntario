package com.example.voluntarios

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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SolicitudTurnoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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



        tvEstado.text = when (turno.estado.lowercase()) {
            "pendiente" -> "Estado del turno: Pendiente"
            "confirmado" -> "Estado del turno: Confirmado"
            "rechazado" -> "Estado del turno: Rechazado"
            else -> "Estado: ${turno.estado}"
        }

        tvMensaje.text = "Muchas gracias ${voluntario.nombre}, ${voluntario.apellido}  por querer participar en el sistema de encuestas. " +
                "Somos $totalVoluntarios voluntarios en total. " +
                "Se te notificará por este medio cuando tu turno haya sido confirmado."

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

                    val turno = turnoViewModel.getTurnoPorUidVoluntario(user.uid)

                    Log.d("SolicitudTurno", "Turno encontrado: $turno")


                    if (turno != null) {
                        mostrarEstado(turno,voluntario,layoutFormulario, cardEstado, tvMensaje, tvEstado)
                    }
                    else {
                        etNombre.setText(voluntario.nombre)
                        etApellido.setText(voluntario.apellido)
                        etFecha.setText(voluntario.fechaNac)
                        layoutFormulario.visibility = View.VISIBLE
                    }
                    }
                else {
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
                Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            if (nombre.isEmpty() || apellido.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val voluntario = turnoViewModel.getVoluntarioByUid(user.uid)
                if (voluntario != null) {
                    val voluntarioActualizado = voluntario.copy(
                        nombre = nombre,
                        apellido = apellido,
                        fechaNac = fecha
                    )

                    voluntarioViewModel.actualizarVoluntario(voluntarioActualizado)

                    val turno = Turno(
                        voluntarioId = null,
                        voluntariouid = user.uid,
                        estado = "pendiente"
                    )
                    turnoViewModel.insertar(turno)

                    // 🔔 SUSCRIPCIÓN AUTOMÁTICA CON TÓPICO PERSONALIZADO
                    val topic = TopicHelper.generarTopic(voluntario.firebaseUid, voluntario.nombre)
                    suscribirseANtfy(topic)

                    Toast.makeText(requireContext(), "Turno solicitado (pendiente)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun actualizarUI(
        turno: Turno,
        voluntario: Voluntario,
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

        tvEstado.text = when (turno.estado.lowercase()) {
            "pendiente" -> "Estado: Pendiente"
            "confirmado" -> "Estado: Confirmado"
            "rechazado" -> "Estado: Rechazado"
            else -> "Estado: ${turno.estado}"
        }

        val mensaje = when (turno.estado) {
            "confirmado" -> buildString {
                append("Muchas gracias ${voluntario.nombre} ${voluntario.apellido}. ")
                append("Somos $totalVoluntarios voluntarios. ")
                append("Tu turno será el ${turno.dia} a las ${turno.horario} en ${turno.direccion}. ")
                append("Instrucciones: ${turno.descripcion}")
            }
            "rechazado" -> "Lamentamos informarte que tu turno ha sido cancelado. Motivo: ${turno.descripcion}"
            else -> "Muchas gracias ${voluntario.nombre} ${voluntario.apellido} por querer participar. " +
                    "Se te notificará cuando tu turno sea confirmado."
        }
        tvMensaje.text = mensaje

        layoutFormulario.visibility = View.GONE
        cardEstado.visibility = View.VISIBLE
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