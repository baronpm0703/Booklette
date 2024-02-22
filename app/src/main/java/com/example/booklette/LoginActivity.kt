package com.example.booklette

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.asLiveData
import com.example.booklette.databinding.ActivityLoginBinding
import com.example.booklette.databinding.ActivityMainBinding
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import rememberMeManager
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    lateinit var remember_me_manager: rememberMeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        remember_me_manager = rememberMeManager(this)

        this.remember_me_manager.rmFlow.asLiveData().observe(this) {
            binding.cbRememberMe.isChecked = it
            if (it) {
                this.remember_me_manager.emailFlow.asLiveData().observe(this) {
                    binding.edtEmailSignIn.setText(it)
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        bindProgressButton(binding.btnLogIn)
        binding.btnLogIn.attachTextChangeAnimator()
        this@LoginActivity.bindProgressButton(binding.btnLogIn)

        binding.txtForgotPassword.setOnClickListener({
            startActivity(Intent(this@LoginActivity, ForgotPassword::class.java))
        })

        binding.btnLogIn.setOnClickListener({
            if (binding.edtEmailSignIn.text.isEmpty()) {
                MotionToast.createColorToast(this@LoginActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyEmailSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else if (binding.edtPasswordSignIn.text.isEmpty()) {
                MotionToast.createColorToast(this@LoginActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyPasswordSignIn),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else {
                binding.btnLogIn.showProgress {
                    buttonTextRes = R.string.signingInText
                    progressColor = Color.WHITE
                }

                auth.signInWithEmailAndPassword(binding.edtEmailSignIn.text.toString(), binding.edtPasswordSignIn.text.toString())
                    .addOnCompleteListener(this) { task ->
                        binding.btnLogIn.hideProgress(R.string.loginText)

                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser

                            MotionToast.createColorToast(this@LoginActivity,
                                getString(R.string.successfully),
                                getString(R.string.signInSuccessDescription),
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))

                            GlobalScope.launch { remember_me_manager.storeUser(binding.cbRememberMe.isChecked, binding.edtEmailSignIn.text.toString()) }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("firebase", task.exception.toString())

                            MotionToast.createColorToast(this@LoginActivity,
                                getString(R.string.failed),
                                getString(R.string.signUpFailedDescription),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
                        }
                    }
            }
        })

        binding.txtBackToSignUpLI.setOnClickListener({
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        })

        binding.ivBackLI.setOnClickListener({
            finish()
        })
    }
}