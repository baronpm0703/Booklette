package com.example.booklette

import android.content.Intent
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle



class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySignUpBinding

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
                                    val linkToStore = "/personalStores/"+ documentReference.id
                                    println("DocumentSnapshot added with ID: $linkToStore")

                                    val dataAccount: HashMap<String, Any> = hashMapOf(
                                        "UID" to user!!.uid,
                                        "address" to "",
                                        "avt" to "",
                                        "blacklist" to emptyArray,
                                        "fullname" to "${binding.edtUsernameSignUp.text}",
                                        "phone" to "",
                                        "shippingAddress" to emptyArray,
                                        "wishlist" to emptyArray,
                                        "store" to linkToStore
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

        binding.ivBackSU.setOnClickListener({
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