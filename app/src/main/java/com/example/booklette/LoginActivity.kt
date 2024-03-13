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
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.firebase.auth.FacebookAuthProvider
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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
import com.facebook.appevents.AppEventsLogger;

open class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    var callbackManager = CallbackManager.Factory.create()
    lateinit var remember_me_manager: rememberMeManager
    var TAG = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this@LoginActivity);
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
//                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        bindProgressButton(binding.btnLogIn)
        binding.btnLogIn.attachTextChangeAnimator()
        this@LoginActivity.bindProgressButton(binding.btnLogIn)

        binding.ivLogInWithFb.setOnClickListener {
            if (userIsLoggedIn()) {
                auth
            } else {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this@LoginActivity, listOf("public_profile", "email"))
            }
            LoginManager.getInstance().registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    // ...
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    // ...
                }
            })
        }

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

    private fun userIsLoggedIn(): Boolean {
        if (auth.currentUser !=null && !AccessToken.getCurrentAccessToken()?.isExpired!!) {
            return true
        }
        return false
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    startActivity(Intent(this, homeActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}