package com.example.booklette

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.booklette.databinding.ActivityHomeBinding
import com.example.booklette.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class homeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
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


//        supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, homeFragment).commit()
        supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, bookDetailFragment).commit()

        binding.smoothBottomBar.setOnItemSelectedListener { item ->
            when (item) {
                0 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, homeFragment).commit()
                    true
                }
                1 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, categoryFragment).commit()
                    true
                }
                2 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, cartFragment).commit()
                    true
                }
//                3 -> {
//                    Toast.makeText(this@homeActivity, "Cart", Toast.LENGTH_SHORT).show()
//                    true
//                }
                4 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fcvNavigation, profileFragment).commit()
                    true
                }
                else -> false
            }
        }

    }
}