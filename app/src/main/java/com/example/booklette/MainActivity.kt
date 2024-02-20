package com.example.booklette

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.booklette.databinding.ActivityMainBinding
import com.example.booklette.ui.theme.BookletteTheme

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnLogIn.setOnClickListener({
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        })

        binding.btnSignUp.setOnClickListener({
            startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
        })

    }
}