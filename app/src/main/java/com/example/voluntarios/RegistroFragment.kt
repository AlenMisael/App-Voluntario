package com.example.voluntarios

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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
import kotlinx.coroutines.launch
import android.text.TextWatcher




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
        val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
        val etEmail = view.findViewById<EditText>(R.id.etEmailRegistro)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordRegistro)
        val btnCrearCuenta = view.findViewById<Button>(R.id.btnCrearCuenta)
        val btnVolverLogin = view.findViewById<Button>(R.id.btnVolverLogin)


        savedInstanceState?.let { bundle ->
            etNombre.setText(bundle.getString("nombre", ""))
            etApellido.setText(bundle.getString("apellido", ""))
            etFechaNac.setText(bundle.getString("fechaNac", ""))
            etTelefono.setText(bundle.getString("telefono", ""))
            etEmail.setText(bundle.getString("email", ""))
        }

        val filtroSoloLetras = InputFilter { source, _, _, _, _, _ ->
            if (source.all { it.isLetter() || it.isWhitespace() }) null else ""
        }

        etNombre.filters = arrayOf(filtroSoloLetras)
        etApellido.filters = arrayOf(filtroSoloLetras)


        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && !validarEmail(email)) {
                    etEmail.error = "El email debe ser @gmail.com, @yahoo.com, @outlook.com o @hotmail.com"
                } else {
                    etEmail.error = null
                }
            }
        })

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


        etFechaNac.setOnClickListener {
            mostrarDatePicker(requireContext(), etFechaNac)
        }

        btnCrearCuenta.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val fechaNac = etFechaNac.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()




            if (etEmail.error != null || !validarEmail(email)) {
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (etTelefono.error != null) {
                etTelefono.requestFocus()
                return@setOnClickListener
            }

            if (nombre.isEmpty() || apellido.isEmpty() || fechaNac.isEmpty() || email.isEmpty() || password.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(requireContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            btnCrearCuenta.isEnabled = false
            btnCrearCuenta.text = "Registrando..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    if (!isAdded) return@addOnSuccessListener

                    val user = auth.currentUser ?: return@addOnSuccessListener

                    val voluntario = Voluntario(
                        firebaseUid = user.uid,
                        nombre = nombre,
                        apellido = apellido,
                        fechaNac = fechaNac,
                        telefono = telefono,
                        email = email
                    )

                    viewLifecycleOwner.lifecycleScope.launch {

                        voluntarioViewModel.insertar(voluntario)

                        context?.let {
                            Toast.makeText(
                                it,
                                "Usuario registrado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        irASolicitudTurno()

                    }


                }
                .addOnFailureListener { e ->
                    if (!isAdded) return@addOnFailureListener

                    btnCrearCuenta.isEnabled = true
                    btnCrearCuenta.text = "Crear cuenta"

                    context?.let {
                        Toast.makeText(
                            it,
                            e.message ?: "Error al registrar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        btnVolverLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun validarEmail(email: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9._%+\\-]+@(gmail|yahoo|outlook|hotmail)\\.com$")
        return regex.matches(email)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        view?.let {
            outState.putString("nombre",   it.findViewById<EditText>(R.id.etNombre).text.toString())
            outState.putString("apellido", it.findViewById<EditText>(R.id.etApellido).text.toString())
            outState.putString("fechaNac", it.findViewById<EditText>(R.id.etFechaNac).text.toString())
            outState.putString("telefono", it.findViewById<EditText>(R.id.etTelefono).text.toString())
            outState.putString("email",    it.findViewById<EditText>(R.id.etEmailRegistro).text.toString())
        }
    }

    private fun irASolicitudTurno() {

        if (!isAdded) return

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SolicitudTurnoFragment())
            .commit()
    }
}