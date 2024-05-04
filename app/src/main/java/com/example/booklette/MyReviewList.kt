package com.example.booklette

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.booklette.databinding.FragmentBookDetailBinding
import com.example.booklette.databinding.FragmentMyReviewListBinding
import com.example.booklette.model.CartObject
import com.example.booklette.model.Photo
import com.example.booklette.model.UserReviewObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
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

    @SuppressLint("NotifyDataSetChanged")
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

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvReviewList)


        myBookReviewListRVAdapter = activity?.let{ MyBookReviewListRVAdapter(it, null)}!!
        binding.rvReviewList.adapter = myBookReviewListRVAdapter

        binding.rvReviewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        binding.cartSwipeRefresh.setOnRefreshListener {
            myBookReviewListRVAdapter.notifyDataSetChanged()
            Handler().postDelayed({
                binding.cartSwipeRefresh.isRefreshing = false;
            }, 500)

        }
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

    private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Handle swiping action here
            val position = viewHolder.adapterPosition
            val adapter = binding.rvReviewList.adapter as? MyBookReviewListRVAdapter
            adapter?.let {
                // Retrieve item information before swiping
                val deletedItemInfo = it.getItemInfo(position)
                // Show delete confirmation dialog
                if (deletedItemInfo != null)
                    showDeleteConfirmationDialog(deletedItemInfo.toString(), position)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val clampedDX = if (Math.abs(dX) > 200) {
                if (dX > 0) 200.toFloat() else -200.toFloat()
            } else {
                dX
            }

            // Vẽ background và icon cho Swipe-To-Remove
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom.toFloat() - itemView.top.toFloat()
            val itemWidth = itemHeight

            val p = Paint()
            if (clampedDX  < 0) {
                p.color = Color.rgb(200, 0, 0) // Màu đỏ
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                val newHeight = itemHeight*0.85f
                val bottomMargin = (itemHeight - newHeight)
                val adjustedBackground = RectF(background.left, background.top + bottomMargin +2.5f, background.right, background.bottom-18)
                c.drawRoundRect(adjustedBackground, 50f, 50f, p)
                val iconSize = 70 // Kích thước của biểu tượng xóa
                val iconMargin = 70// Chuyển đổi itemHeight sang kiểu Int
                val iconLeft = (itemView.right - iconMargin - iconSize ).toInt()
                val iconTop = (itemView.top + (itemHeight - iconSize) / 2 + 26f).toInt()
                val iconRight = (itemView.right - iconMargin).toInt()
                val iconBottom = (iconTop + iconSize).toInt()
                val deleteIcon = resources.getDrawable(R.drawable.ic_delete) // Thay R.drawable.ic_delete bằng ID của biểu tượng xóa thực tế
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    private fun showDeleteConfirmationDialog(deletedItemInfo: String, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to delete this item?")

        builder.setPositiveButton("Delete") { dialog, which ->
            val adapter = binding.rvReviewList.adapter as? MyBookReviewListRVAdapter
            val reviewObject = adapter?.getItemInfo(position)
            reviewObject?.let { reviewItem ->
                deleteReviewItem(reviewItem, position)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()

        val currentPosition = (binding.rvReviewList.adapter as? MyBookReviewListRVAdapter)?.getItemInfo(position)

        alertDialog.setOnCancelListener {
            currentPosition?.let { restoredItem ->
                val adapter = binding.rvReviewList.adapter as? MyBookReviewListRVAdapter
                adapter?.notifyItemChanged(position)
            }
        }

        alertDialog.show()
    }
    private fun deleteReviewItem(item: Pair<String, UserReviewObject>, position: Int) {
        db.collection("books")
            .whereEqualTo("bookID", item.first)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val updateTask = FirebaseFirestore.getInstance().runTransaction { transaction ->
                        val documentSnapshot = transaction.get(document.reference)
                        val reviewList = documentSnapshot.data?.get("review") as? MutableList<MutableMap<String, Any?>> ?: mutableListOf()

                        // Find the index of the element to delete
                        val indexToDelete = reviewList.indexOfFirst { it["UID"] as? String == item.second.userID }

                        if (indexToDelete != -1) {
                            reviewList.removeAt(indexToDelete)
                            transaction.update(document.reference, "review", reviewList)  // Update the document with the modified list
                        } else {
                            // Handle case where element not found (optional)
                            Log.d("Firebase delete book", "Element with UID not found")
                        }

                        // Complete the transaction (empty block)
                    }

                    updateTask.addOnSuccessListener {
                            Log.d("Firebase delete book", "Element deleted successfully")
                            myBookReviewListRVAdapter.removeItem(position)
//                            myBookReviewListRVAdapter.notifyItemRemoved(position)
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firebase delete book", "Error deleting element: ", exception)
                        }
                }
                MotionToast.createColorToast(
                    context as Activity,
                    getString(R.string.delete_cart_sucessfuly),
                    getString(R.string.delete_notification),
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular))
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
            }
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