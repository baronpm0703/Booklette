package com.example.booklette

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.booklette.databinding.ActivitySignUpBinding
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        bindProgressButton(binding.btnSignUpSubmit)
        binding.btnSignUpSubmit.attachTextChangeAnimator()
        this@SignUpActivity.bindProgressButton(binding.btnSignUpSubmit)

        binding.btnSignUpSubmit.setOnClickListener({
            if (binding.edtUsernameSignUp.equals("")) {
                MotionToast.createColorToast(this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyUsernameSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else if (binding.edtEmailSignUp.equals("")) {
                MotionToast.createColorToast(this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyEmailSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else if (!isValidEmail(binding.edtEmailSignUp.text.toString())) {
                MotionToast.createColorToast(this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.emailNotValid),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else if (binding.edtPasswordSignUp.text.length < 6) {
                MotionToast.createColorToast(this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.passwordCheckSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else if (binding.edtPasswordSignUp.text.toString() != binding.edtConfirmPasswordSignUp.text.toString()) {
                MotionToast.createColorToast(this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.confirmPasswordCheck),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            else {
                binding.btnSignUpSubmit.showProgress {
                    buttonTextRes = R.string.signingUpText
                    progressColor = Color.WHITE
                }

                auth.createUserWithEmailAndPassword(binding.edtEmailSignUp.text.trim().toString(), binding.edtConfirmPasswordSignUp.text.toString())
                    .addOnCompleteListener(this) { task ->
                        binding.btnSignUpSubmit.hideProgress(R.string.signUpText)

                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            MotionToast.createColorToast(this@SignUpActivity,
                                "Successfully",
                                "You have successfully created account!",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
                        } else {
                            Log.d("firebase", task.exception.toString())

                            MotionToast.createColorToast(this@SignUpActivity,
                                "Failed",
                                task.exception.toString(),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular))
                        }
                    }
            }
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