package com.example.booklette

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.core.content.res.ResourcesCompat
import com.example.booklette.databinding.ActivityForgotPasswordBinding
import com.example.booklette.databinding.ActivityMainBinding
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        bindProgressButton(binding.btnForgotPasswordSubmit)
        binding.btnForgotPasswordSubmit.attachTextChangeAnimator()
        this@ForgotPassword.bindProgressButton(binding.btnForgotPasswordSubmit)

        binding.btnForgotPasswordSubmit.setOnClickListener({

            if (isValidEmail(binding.edtForgotEmail.text.toString())) {
                binding.btnForgotPasswordSubmit.showProgress {
                    buttonTextRes = R.string.sendingText
                    progressColor = Color.WHITE
                }

                auth.sendPasswordResetEmail(binding.edtForgotEmail.text.toString()).addOnCompleteListener({
                    binding.btnForgotPasswordSubmit.hideProgress(R.string.forgotPassSubmit)

                    if (it.isSuccessful) {
                        MotionToast.createColorToast(this@ForgotPassword,
                            getString(R.string.successfully),
                            getString(R.string.forgotPasswordSuccess),
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
                    }
                    else {
                        MotionToast.createColorToast(this@ForgotPassword,
                            getString(R.string.failed),
                            getString(R.string.signUpFailedDescription),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
                    }
                })
            }
            else {
                MotionToast.createColorToast(this@ForgotPassword,
                    getString(R.string.failed),
                    getString(R.string.emailNotValid),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
        })

        binding.txtBackToLoginFP.setOnClickListener({
            finish()
        })

        binding.ivBackFP.setOnClickListener({
            finish()
        })
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }
}