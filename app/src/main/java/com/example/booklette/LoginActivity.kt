package com.example.booklette

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.booklette.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
//import rememberMeManager
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


open class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    val defaultAvt = "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Accounts%2Fdefault.png?alt=media&token=fd80f83e-7717-4279-a090-4dc97fa435b9"
    val defaultStoreAvt = "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/personalStoresAvatar%2Fbook_shop_avatar.png?alt=media&token=5a3e58cc-56ae-4b8f-9b90-a9ee825ec9ee"

    var callbackManager = CallbackManager.Factory.create()
//    lateinit var remember_me_manager: rememberMeManager

    var TAG = ""

    private lateinit var googleAuth: GoogleSignInClient
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    private var oneTapClient: SignInClient? = null
    private var signInRequest: BeginSignInRequest? = null

    companion object {
        private const val RC_SIGN_IN = 9001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this@LoginActivity);
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.firestore

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)

        if (rememberMe) {
            val email = sharedPreferences.getString("email", "")
            binding.edtEmailSignIn.setText(email)

            binding.cbRememberMe.isChecked = rememberMe
        }

//        remember_me_manager = rememberMeManager(this)
//
//        this.remember_me_manager.rmFlow.asLiveData().observe(this) {
//            binding.cbRememberMe.isChecked = it
//            if (it) {
//                this.remember_me_manager.emailFlow.asLiveData().observe(this) {
//                    binding.edtEmailSignIn.setText(it)
////                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        bindProgressButton(binding.btnLogIn)
        binding.btnLogIn.attachTextChangeAnimator()
        this@LoginActivity.bindProgressButton(binding.btnLogIn)

        // Login with FB
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

                    finish()
                    startActivity(Intent(this@LoginActivity, homeActivity::class.java))
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

        // Login with GG
        binding.ivLogInWithGoogle.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Forgot Pass
        binding.txtForgotPassword.setOnClickListener({
            startActivity(Intent(this@LoginActivity, ForgotPassword::class.java))
        })

        // Normal Login
        binding.btnLogIn.setOnClickListener {
            if (binding.edtEmailSignIn.text.isEmpty()) {
                MotionToast.createColorToast(
                    this@LoginActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyEmailSignUp),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else if (binding.edtPasswordSignIn.text.isEmpty()) {
                MotionToast.createColorToast(
                    this@LoginActivity,
                    getString(R.string.failed),
                    getString(R.string.emptyPasswordSignIn),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else {
                binding.btnLogIn.showProgress {
                    buttonTextRes = R.string.signingInText
                    progressColor = Color.WHITE
                }

                auth.signInWithEmailAndPassword(
                    binding.edtEmailSignIn.text.toString(),
                    binding.edtPasswordSignIn.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        binding.btnLogIn.hideProgress(R.string.loginText)

                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser

                            MotionToast.createColorToast(
                                this@LoginActivity,
                                getString(R.string.successfully),
                                getString(R.string.signInSuccessDescription),
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    this,
                                    www.sanju.motiontoast.R.font.helvetica_regular
                                )
                            )

//                            GlobalScope.launch { remember_me_manager.storeUser(binding.cbRememberMe.isChecked, binding.edtEmailSignIn.text.toString()) }
                            val editor = sharedPreferences.edit()

                            editor.putBoolean("rememberMe", binding.cbRememberMe.isChecked)
                            editor.putString("email", binding.edtEmailSignIn.text.toString())

                            editor.apply()

                            val StreamUser = User(
                                id = user!!.uid
                            )
                            val client = ChatClient.Builder("egx6qn892ejq", applicationContext)
                                .logLevel(ChatLogLevel.ALL)
                                .build()

                            lifecycleScope.launch {
                                client.connectUser(
                                    user = StreamUser,
                                    token = client.devToken(StreamUser.id)
                                ).enqueue()

                                finish()
                                startActivity(Intent(this@LoginActivity, homeActivity::class.java))
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("firebase", task.exception.toString())

                            MotionToast.createColorToast(
                                this@LoginActivity,
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

        // Back to SignUp
        binding.txtBackToSignUpLI.setOnClickListener({
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        })

        //
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

        var isExisted = false
        when (requestCode) {
            RC_SIGN_IN -> {
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
                                                "storeAvatar" to defaultStoreAvt,
                                                "storeLocation" to "",
                                                "email" to user?.email.toString(),
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
                                                        "avt" to defaultAvt,
                                                        "dob" to Timestamp.now(),
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
                                            startActivity(Intent(this@LoginActivity, homeActivity::class.java))
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


}