package com.example.voluntarios

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = AppDatabase.getInstance(requireContext())

        val etEmail = view.findViewById<EditText>(R.id.editTextTextEmailAddress)
        val etPassword = view.findViewById<EditText>(R.id.editTextTextPassword)
        val btnIngresar = view.findViewById<Button>(R.id.button)
        val btnRegistrar = view.findViewById<Button>(R.id.button2)
        val btnGoogle = view.findViewById<SignInButton>(R.id.btnGoogle)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        btnIngresar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completá email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null) {
                        guardarVoluntarioLocalSiNoExiste(
                            uid = user.uid,
                            email = user.email ?: email,
                            displayName = user.displayName ?: email.substringBefore("@")
                        )
                    }
                    Toast.makeText(requireContext(), "Bienvenido", Toast.LENGTH_SHORT).show()
                    irASolicitudTurno()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), e.message ?: "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
        }

        btnRegistrar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegistroFragment())
                .commit()
        }

        btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "Login con Google cancelado", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                val idToken = account.idToken
                if (idToken == null) {
                    Toast.makeText(requireContext(), "No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                    val credential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        val user = auth.currentUser
                        if (user != null) {
                            guardarVoluntarioLocalSiNoExiste(
                                uid = user.uid,
                                email = user.email ?: account.email.orEmpty(),
                                displayName = user.displayName ?: account.displayName.orEmpty()
                            )
                        }
                        Toast.makeText(requireContext(), "Bienvenido con Google", Toast.LENGTH_SHORT).show()
                        irASolicitudTurno()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), e.message ?: "Error en login con Google", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Error en login con Google", Toast.LENGTH_SHORT).show()
            }
        }

    private fun guardarVoluntarioLocalSiNoExiste(
        uid: String,
        email: String,
        displayName: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val existente = db.voluntarioDao().getByUid(uid)

            if (existente == null) {
                val partes = displayName.trim().split(" ").filter { it.isNotBlank() }
                val nombre = partes.firstOrNull() ?: "Usuario"
                val apellido = if (partes.size > 1) partes.drop(1).joinToString(" ") else ""

                val voluntario = Voluntario(
                    firebaseUid = uid,
                    nombre = nombre,
                    apellido = apellido,
                    fechaNac = "",
                    email = email
                )

                db.voluntarioDao().insert(voluntario)
            }
        }
    }

    private fun irASolicitudTurno() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SolicitudTurnoFragment())
            .commit()
    }
}