package com.example.voluntarios

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main)

        if (savedInstanceState == null) {

            val user = FirebaseAuth.getInstance().currentUser

            val fragmentInicial = if (user != null) {
                SolicitudTurnoFragment()
            } else {
                LoginFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentInicial)
                .commit()
        }
    }
}