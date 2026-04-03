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

import androidx.fragment.app.viewModels

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        auth = FirebaseAuth.getInstance()

        val btnGuardarTurno = view.findViewById<Button>(R.id.btnGuardarTurno)

        btnCerrarSesion.setOnClickListener {
            auth.signOut()

            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }

        btnGuardarTurno.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            viewLifecycleOwner.lifecycleScope.launch {
                val voluntario = turnoViewModel.getVoluntarioByUid(user.uid)

                if (voluntario != null) {
                    val turno = Turno(
                        voluntarioId = voluntario.id,
                        estado = "pendiente"
                    )

                    turnoViewModel.insertar(turno)

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
