package com.example.booklette

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.booklette.databinding.ActivityMainBinding
import com.example.booklette.databinding.ActivityVerifyEmailBinding

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnVerifyOTPSubmit.setOnClickListener({
            startActivity(Intent(this@VerifyEmailActivity, CreateNewPasswordActivity::class.java))
        })
    }
}