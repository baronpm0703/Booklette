package com.example.booklette

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.booklette.databinding.ActivityHomeBinding
import com.example.booklette.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class homeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        if (auth.currentUser != null) {
            binding.txtWelcomeBack.text = "Welcome back, " + auth.currentUser!!.email.toString()
        }

        binding.btnSignOut.setOnClickListener({
            auth.signOut()
        })
    }
}