package com.example.booklette

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.booklette.databinding.ActivityLoginBinding
import com.example.booklette.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.txtForgotPassword.setOnClickListener({
            startActivity(Intent(this@LoginActivity, ForgotPassword::class.java))
        })
    }
}