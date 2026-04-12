package com.example.voluntarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RegistroFragment : Fragment() {

    private val voluntarioViewModel: VoluntarioViewModel by viewModels {
        VoluntarioViewModel.VoluntarioViewModelFactory(
            (requireActivity().application as AppVoluntarios).voluntarioRepositorio
        )
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_registro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etApellido = view.findViewById<EditText>(R.id.etApellido)
        val etFechaNac = view.findViewById<EditText>(R.id.etFechaNac)
        val etEmail = view.findViewById<EditText>(R.id.etEmailRegistro)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordRegistro)
        val btnCrearCuenta = view.findViewById<Button>(R.id.btnCrearCuenta)
        val btnVolverLogin = view.findViewById<Button>(R.id.btnVolverLogin)


        etFechaNac.setOnClickListener {
            mostrarDatePicker(requireContext(), etFechaNac)
        }

        btnCrearCuenta.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val fechaNac = etFechaNac.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()



            if (nombre.isEmpty() || apellido.isEmpty() || fechaNac.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null) {
                        val voluntario = Voluntario(
                            firebaseUid = user.uid,
                            nombre = nombre,
                            apellido = apellido,
                            fechaNac = fechaNac,
                            email = email
                        )

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            voluntarioViewModel.insertar(voluntario)
                        }
                    }

                    Toast.makeText(requireContext(), "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    irASolicitudTurno()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), e.message ?: "Error al registrar", Toast.LENGTH_SHORT).show()
                }
        }

        btnVolverLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun irASolicitudTurno() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SolicitudTurnoFragment())
            .commit()
    }
}