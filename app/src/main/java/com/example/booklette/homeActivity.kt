package com.example.booklette

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.ActivityHomeBinding
import com.example.booklette.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class homeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    var smoothBottomBarStack = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val homeFragment = HomeFragment()
        val categoryFragment = CategoryFragment()
        val productList = ProductList()
        val cartFragment = CartFragment()
        val bookDetailFragment = BookDetailFragment()
        // (Khoa) tui gán này là để test My Shop thôi nha (do chưa có My Profile mà My Shop phải đi từ đó vào). Khi nào có My Profile thì đổi
        val profileFragment = MyShopFragment()


        supportFragmentManager.beginTransaction().add(R.id.fcvNavigation, homeFragment).commit()
        smoothBottomBarStack.add(0)
//        supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, bookDetailFragment).commit()

        binding.smoothBottomBar.setOnItemSelectedListener { item ->
            when (item) {
                0 -> {
                    changeFragmentContainer(homeFragment, 0)
                    true
                }
                1 -> {
                    changeFragmentContainer(categoryFragment, 1)
                    true
                }
                2 -> {
                    changeFragmentContainer(cartFragment, 2)
                    true
                }
//                3 -> {
//                    Toast.makeText(this@homeActivity, "Cart", Toast.LENGTH_SHORT).show()
//                    true
//                }
                4 -> {
                    changeFragmentContainer(profileFragment, 4)
                    true
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            smoothBottomBarStack.removeAt(smoothBottomBarStack.size - 1)

            if (smoothBottomBarStack.size > 0) {
                binding.smoothBottomBar.itemActiveIndex = smoothBottomBarStack[smoothBottomBarStack.size - 1]
//                Toast.makeText(this@homeActivity, binding.smoothBottomBar.itemActiveIndex.toString(), Toast.LENGTH_SHORT).show()
            }
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            isEnabled = true
        }
    }

    fun changeFragmentContainer(fragment: Fragment, barItem: Int) {
        supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
        smoothBottomBarStack.add(barItem)
    }
}