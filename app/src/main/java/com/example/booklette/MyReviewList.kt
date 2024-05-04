package com.example.booklette

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.booklette.databinding.FragmentBookDetailBinding
import com.example.booklette.databinding.FragmentMyReviewListBinding
import com.example.booklette.model.Photo
import com.example.booklette.model.UserReviewObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyReviewList.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyReviewList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMyReviewListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var userReviewInfo: ArrayList<Pair<String, UserReviewObject>>? = null
    private lateinit var myBookReviewListRVAdapter: MyBookReviewListRVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyReviewListBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage("gs://book-store-3ed32.appspot.com")

        lifecycleScope.launch {
            userReviewInfo = auth.uid?.let { getReviewInfoOfUser(it) }
            userReviewInfo?.let {
                myBookReviewListRVAdapter.updateReviewList(it)
                myBookReviewListRVAdapter.notifyDataSetChanged()
            }
        }

        myBookReviewListRVAdapter = activity?.let{ MyBookReviewListRVAdapter(it, null)}!!
        binding.rvReviewList.adapter = myBookReviewListRVAdapter

        binding.rvReviewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        return view
    }

    suspend fun getReviewInfoOfUser(UID: String): ArrayList<Pair<String, UserReviewObject>>? {
        var res: ArrayList<Pair<String, UserReviewObject>>? = null
        try {
            val docRef = db.collection("accounts").whereEqualTo("UID", UID).get().await()

            for (doc in docRef.documents) {
                val uid = doc.data?.get("UID").toString()
                val username = doc.data?.get("fullname").toString()
                val avatar = doc.data?.get("avt").toString()

                var rating: Float
                var cmt: String
                var linkToFolder: String
                val bookRef = db.collection("books").whereNotEqualTo("bookID", null).get().await()

                val x = 1
                for (book in bookRef.documents) {
                    if (book.data?.get("review") != null) {

                        val reviewsArray =
                            book.data!!["review"] as ArrayList<Map<String, Any>>

                        if (reviewsArray.isNotEmpty()) {

                            val reviewOfUser = reviewsArray.firstOrNull { it["UID"] == UID}
                            if (reviewOfUser != null) {
                                rating = reviewOfUser["score"].toString().toFloat()
                                cmt = reviewOfUser["text"].toString()
                                linkToFolder = reviewOfUser["image"].toString()

                                val reviewInfo = UserReviewObject(uid, username, avatar, rating, cmt)
                                if (linkToFolder.isNotEmpty()) {
                                    reviewInfo.reviewPhotos = downloadImagesFromFolder(linkToFolder)
                                }

                                if (res == null) {
                                    res = ArrayList()

                                }
                                res.add(Pair(book.data!!["bookID"].toString(), reviewInfo))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception){
            Log.e("Firebase Error", "Can't retrieve data")
        }
        return res
    }

    fun downloadImagesFromFolder(folderPath: String): ArrayList<Photo>{
        val storage = Firebase.storage

        val listRef =  storage.getReferenceFromUrl(folderPath)

        val dataPhoto = ArrayList<Photo>()

        listRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->

                    val originalFilename = item.name

                    // Download the file directly into a Bitmap
                    item.getBytes(Long.MAX_VALUE)
                        .addOnSuccessListener { bytes ->
                            // Convert the downloaded bytes to a Bitmap
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val photo = Photo()
                            photo.id = Photo.totalID
                            photo.nameFile = item.name
                            photo.id?.let { Photo.updateTotalID(it) }
                            photo.image = bitmap
                            dataPhoto.add(photo)
                            // Now you can use the bitmap for further processing
                            // For example, you can display it in an ImageView
                            // imageView.setImageBitmap(bitmap)
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors
                            println("Failed to download $originalFilename: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                println("Failed to list files in folder: $exception")
            }

        return dataPhoto
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyReviewList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyReviewList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}