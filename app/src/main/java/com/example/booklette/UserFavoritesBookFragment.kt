package com.example.booklette

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentUserFavoritesBookBinding
import com.example.booklette.model.BookObject
import com.example.booklette.model.UserReviewObject
import com.example.booklette.model.UserWishListObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFavoritesBookFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFavoritesBookFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentUserFavoritesBookBinding? = null
    private lateinit var rvUserFavoritesBookAdapter: UserFavoritesBookRVAdapter
    private var wishList: ArrayList<BookObject> = ArrayList()
    private var userInfo: UserWishListObject? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserFavoritesBookBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        auth = Firebase.auth
        lifecycleScope.launch {
            userInfo = auth.uid?.let { getInfoCurrentUser(it) }
            wishList = ArrayList(userInfo!!.wishList)
            rvUserFavoritesBookAdapter.updateWishList(userInfo!!.wishList)
            rvUserFavoritesBookAdapter.notifyDataSetChanged()
        }

//        val bookObject = BookObject("123", "HEllo", "Novel", "KKK", "09/01/2023","",100.0F, "123")
//        wishList.add(bookObject)

        rvUserFavoritesBookAdapter = activity?.let { UserFavoritesBookRVAdapter(it, userInfo?.wishList) }!!

        binding.rvWishList.adapter = rvUserFavoritesBookAdapter
        binding.rvWishList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(v: RecyclerView, h: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(h: RecyclerView.ViewHolder, dir: Int)  {
                val position = h.adapterPosition
                rvUserFavoritesBookAdapter.removeAt(position)
                wishList.removeAt(position)
                empTyWishList(wishList)

                MotionToast.createColorToast(
                    context as Activity,
                    getString(R.string.successfully),
                    getString(R.string.delete_notification),
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
        }).attachToRecyclerView(binding.rvWishList)

//        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
//        itemTouchHelper.attachToRecyclerView(binding.rvWishList)

        binding.deleteAllBtn.setOnClickListener {
            wishList.clear()
            rvUserFavoritesBookAdapter.updateWishList(wishList)
            rvUserFavoritesBookAdapter.notifyDataSetChanged()
            empTyWishList(wishList) // Update database
        }


        // Inflate the layout for this fragment
        return view
    }

    fun empTyWishList(wishList: ArrayList<BookObject>){
        var item = ArrayList<String>()
        for (book in wishList) item.add(book.bookID)

        val docAccountRef = db.collection("accounts").whereEqualTo("UID", userInfo?.userID)

        docAccountRef.get().addOnSuccessListener {
            for (document in it) {
                document.reference.update("wishlist", item)
                    .addOnSuccessListener {
                        Log.i("update Wish List", "Success")
                    }
                    .addOnFailureListener {
                        Log.i("update Wish List", "Failed")
                    }
            }
        }
    }

    suspend fun getBookPrice1(bookID: String): Float {
        return try {
            val querySnapshot = db.collection("personalStores").whereNotEqualTo("items.$bookID.price", null).get().await()
            for (document in querySnapshot.documents) {
                val bookList = document.data?.get("items") as? Map<String, Any>
                val bookDetail = bookList?.get(bookID) as? Map<String, Any>

                val price = bookDetail?.get("price").toString()
                if (!price.isNullOrEmpty()) {
                    return price.toFloat()
                }
            }
            // Return a default value if the book price is not found
            0.0F
        } catch (e: Exception) {
            // Handle failures
            Log.e("Firestore", "Error getting book price", e)
            // Return a default value in case of failure
            0.0F
        }
    }
    suspend fun getInfoCurrentUser(UID: String): UserWishListObject? {
        var res: UserWishListObject? = null
        try {
            val docRef = db.collection("accounts").whereEqualTo("UID", UID).get().await()

            for (doc in docRef.documents) {
                val uid = doc.data?.get("UID").toString()
                val username = doc.data?.get("fullname").toString()
                val wishListBookID = doc.data?.get("wishlist") as ArrayList<*>?
                var wishList: ArrayList<BookObject> = ArrayList()

                if (wishListBookID != null) {
                    wishListBookID.forEach { // ID
                        val bookRef = db.collection("books").whereEqualTo("bookID", it).get().await()
                        for (book in bookRef.documents) {
                            val tmp = getBookPrice1(it.toString())
                            val bookObject = BookObject(it.toString(), book.data?.get("name").toString(), book.data?.get("genre").toString(), username, "", book.data?.get("image").toString(), tmp, "")
                            wishList.add(bookObject)
                        }
                    }
                }

                res = UserWishListObject(uid, username)
                res.wishList = wishList
            }
        } catch (e: Exception){
            Log.e("Firebase Error", "Can't retrieve data")
            val docRef = db.collection("accounts").whereEqualTo("UID", UID).get().await()

            for (doc in docRef.documents) {
                val uid = doc.data?.get("UID").toString()
                val username = doc.data?.get("fullname").toString()

                res = UserWishListObject(uid, username)
                var wishList: ArrayList<BookObject> = ArrayList()
                res.wishList = wishList
            }
        }

        return res
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFavoritesBookFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFavoritesBookFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}