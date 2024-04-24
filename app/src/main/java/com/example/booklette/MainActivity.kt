package com.example.booklette

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.booklette.databinding.ActivityMainBinding
import com.example.booklette.ui.theme.BookletteTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
//            startActivity(Intent(this@MainActivity, homeActivity::class.java))
            startActivity(Intent(this@MainActivity, ChannelChatActivity::class.java))
            finish()
        }

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