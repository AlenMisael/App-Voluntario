package com.example.voluntarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolicitudTurnoFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_solicitud_turno, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = AppDatabase.getInstance(requireContext())

        val etDia = view.findViewById<EditText>(R.id.etDia)
        val etHorario = view.findViewById<EditText>(R.id.etHorario)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnGuardarTurno = view.findViewById<Button>(R.id.btnGuardarTurno)

        btnGuardarTurno.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dia = etDia.text.toString().trim()
            val horario = etHorario.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

            if (dia.isEmpty() || horario.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(requireContext(), "Completá día, horario y dirección", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val turno = Turno(
                firebaseUid = user.uid,
                dia = dia,
                horario = horario,
                direccion = direccion,
                descripcion = descripcion,
                estado = "pendiente"
            )

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.turnoDao().insert(turno)
                }

                Toast.makeText(
                    requireContext(),
                    "Turno creado en estado pendiente",
                    Toast.LENGTH_SHORT
                ).show()

                etDia.text.clear()
                etHorario.text.clear()
                etDireccion.text.clear()
                etDescripcion.text.clear()
            }
        }
    }
}