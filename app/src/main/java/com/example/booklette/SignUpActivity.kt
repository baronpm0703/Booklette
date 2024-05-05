package com.example.booklette

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.booklette.databinding.ActivitySignUpBinding
import com.facebook.CallbackManager
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.User
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle



class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySignUpBinding
    var callbackManager = CallbackManager.Factory.create()
    companion object {
        private const val RC_SIGN_UP = 1009
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.firestore

        bindProgressButton(binding.btnSignUpSubmit)
        binding.btnSignUpSubmit.attachTextChangeAnimator()
        this@SignUpActivity.bindProgressButton(binding.btnSignUpSubmit)

        binding.btnSignUpSubmit.setOnClickListener {
            if (binding.edtUsernameSignUp.equals("")) {
                MotionToast.createColorToast(
                    this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyUsernameSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else if (binding.edtEmailSignUp.equals("")) {
                MotionToast.createColorToast(
                    this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyEmailSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else if (!isValidEmail(binding.edtEmailSignUp.text.toString())) {
                MotionToast.createColorToast(
                    this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.emailNotValid),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
            else if (binding.edtPasswordSignUp.text.length < 6) {
                MotionToast.createColorToast(
                    this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.passwordCheckSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else if (binding.edtPasswordSignUp.text.toString() != binding.edtConfirmPasswordSignUp.text.toString()) {
                MotionToast.createColorToast(
                    this@SignUpActivity,
                    getString(R.string.failed),
                    getString(R.string.confirmPasswordCheck),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else {
                binding.btnSignUpSubmit.showProgress {
                    buttonTextRes = R.string.signingUpText
                    progressColor = Color.WHITE
                }

                auth.createUserWithEmailAndPassword(
                    binding.edtEmailSignUp.text.trim().toString(),
                    binding.edtConfirmPasswordSignUp.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        binding.btnSignUpSubmit.hideProgress(R.string.signUpText)

                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val emptyArray = emptyList<Any>()
                            val emptyMap = emptyMap<Any, Any>()

                            val dataPersonalStore: HashMap<String, Any> = hashMapOf(
                                "followers" to 0,
                                "following" to 0,
                                "items" to emptyMap,
                                "rating" to emptyArray,
                                "shopVouchers" to emptyArray,
                                "storeAvatar" to "",
                                "storeLocation" to "",
                                "storeName" to "Seller ${binding.edtUsernameSignUp.text}",
                            )

                            db.collection("personalStores")
                                .add(dataPersonalStore)
                                .addOnSuccessListener { documentReference ->
                                    // Document added successfully, you can get its ID here
                                    val storeRef = db.collection("personalStores").document(documentReference.id)

                                    val dataAccount: HashMap<String, Any> = hashMapOf(
                                        "UID" to user!!.uid,
                                        "address" to "",
                                        "avt" to "",
                                        "blacklist" to emptyArray,
                                        "fullname" to "${binding.edtUsernameSignUp.text}",
                                        "phone" to "",
                                        "shippingAddress" to emptyArray,
                                        "wishlist" to emptyArray,
                                        "store" to storeRef
                                    )
                                    db.collection("accounts").add(dataAccount).addOnCompleteListener {
                                        println("New account has been added: ${documentReference.id}")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle any errors
                                    println("Error adding document: $e")
                                }



                            MotionToast.createColorToast(
                                this@SignUpActivity,
                                getString(R.string.successfully),
                                getString(R.string.signUpSuccessDescription),
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    this,
                                    www.sanju.motiontoast.R.font.helvetica_regular
                                )
                            )


                        } else {
                            Log.d("firebase", task.exception.toString())

                            MotionToast.createColorToast(
                                this@SignUpActivity,
                                getString(R.string.failed),
                                getString(R.string.signUpFailedDescription),
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    this,
                                    www.sanju.motiontoast.R.font.helvetica_regular
                                )
                            )
                        }
                    }
            }
        }

        binding.txtBackToLogInSU.setOnClickListener({
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        })

        binding.signUpWithGG.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_UP)
        }

        binding.ivBackSU.setOnClickListener({
            finish()
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var isExisted = false
        when (requestCode) {
            RC_SIGN_UP -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val idToken = task.getResult(ApiException::class.java).idToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val beforeSignInUser = auth.currentUser
                            if (beforeSignInUser != null) {
                                isExisted = true
                            }
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")

                                        val user = auth.currentUser
                                        // If before login with google the email isn't exist
                                        if (!isExisted) {
                                            val emptyArray = emptyList<Any>()
                                            val emptyMap = emptyMap<Any, Any>()

                                            val dataPersonalStore: HashMap<String, Any> = hashMapOf(
                                                "followers" to 0,
                                                "following" to 0,
                                                "items" to emptyMap,
                                                "rating" to emptyArray,
                                                "shopVouchers" to emptyArray,
                                                "storeAvatar" to "",
                                                "storeLocation" to "",
                                                "storeName" to "Seller ${user?.email.toString()}",
                                            )

                                            db.collection("personalStores")
                                                .add(dataPersonalStore)
                                                .addOnSuccessListener { documentReference ->
                                                    // Document added successfully, you can get its ID here
                                                    val storeRef = db.collection("personalStores").document(documentReference.id)

                                                    val dataAccount: HashMap<String, Any> = hashMapOf(
                                                        "UID" to user!!.uid,
                                                        "address" to "",
                                                        "avt" to "",
                                                        "blacklist" to emptyArray,
                                                        "fullname" to user.email.toString(),
                                                        "phone" to "",
                                                        "shippingAddress" to emptyArray,
                                                        "wishlist" to emptyArray,
                                                        "store" to storeRef
                                                    )
                                                    db.collection("accounts").add(dataAccount).addOnCompleteListener {
                                                        println("New account has been added: ${documentReference.id}")
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    // Handle any errors
                                                    println("Error adding document: $e")
                                                }
                                        }
                                        val StreamUser = User(
                                            id = user!!.uid
                                        )
                                        val client = ChatClient.Builder("egx6qn892ejq", applicationContext)
                                            .logLevel(ChatLogLevel.ALL)
                                            .build()

                                        lifecycleScope.launch {
                                            client.connectUser(user = StreamUser, token = client.devToken(StreamUser.id)).enqueue()

                                            finish()
                                            startActivity(Intent(this@SignUpActivity, homeActivity::class.java))
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                    }
                                }
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    // ...
                }
            }
            else -> {
                // Pass the activity result back to the Facebook SDK
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }
}