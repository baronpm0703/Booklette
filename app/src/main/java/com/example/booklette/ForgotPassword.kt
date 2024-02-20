package com.example.booklette

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.booklette.databinding.ActivityForgotPasswordBinding
import com.example.booklette.databinding.ActivityMainBinding

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnForgotPasswordSubmit.setOnClickListener({
            startActivity(Intent(this@ForgotPassword, VerifyEmailActivity::class.java))
        })
    }
}