package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.Photo
import com.example.booklette.model.UserReviewObject
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import android.os.Handler

class MyBookReviewListRVAdapter(
    private val context: Context,
    private var reviewList: ArrayList<Pair<String, UserReviewObject>>?
): RecyclerView.Adapter<MyBookReviewListRVAdapter.ViewHolder>() {
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var userInfo: UserReviewObject
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val bookCover = listItemView.findViewById<ImageView>(R.id.ivBooKCover)
        val genre = listItemView.findViewById<TextView>(R.id.txtBookGenreNA)
        val bookName = listItemView.findViewById<TextView>(R.id.txtBookNameNA)
        val rating = listItemView.findViewById<per.wsj.library.AndRatingBar>(R.id.topBookRating)
        val owner = listItemView.findViewById<TextView>(R.id.txtBookAuthorNA)
        val review = listItemView.findViewById<TextView>(R.id.txtReview)
        val moreDetail = listItemView.findViewById<CardView>(R.id.moreDetailBtn)

    }


    fun updateReviewList(reviewList: ArrayList<Pair<String, UserReviewObject>>){
        this.reviewList = reviewList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val wishlistView = inflater.inflate(R.layout.my_profile_review_list_fragment_rv_item, parent, false)
        db = Firebase.firestore
        storage = Firebase.storage("gs://book-store-3ed32.appspot.com")
        userInfo = reviewList?.get(0)!!.second

        return ViewHolder(wishlistView)
    }

    override fun getItemCount(): Int {
        return reviewList?.size ?: 0
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = reviewList?.get(position)
        val bookID = item?.first
        val reviewDetail = item?.second

//        Log.i("GV", bookInfo.name)
        if (item != null) {
            var genre = ""
            var name = ""
            var author = ""
            var rating = 0.0F
            var image = ""
            var descriptions = ""

            // Fetch data
            db.collection("books").whereEqualTo("bookID", bookID).get().addOnSuccessListener { documents ->
                for (book in documents){
                    genre = book.data["genre"].toString()
                    name =  book.data["name"].toString()
                    author = book.data["author"].toString()
                    image = book.data["image"].toString()
                    rating = reviewDetail?.ratings!!
                    descriptions = reviewDetail.description
                }

                holder.genre?.text = genre
                holder.bookName?.text = name
                holder.owner?.text = author
                holder.rating.rating = rating
                holder.review?.text = descriptions

                if (image.isNotEmpty()) {
                    Picasso.get()
                        .load(image)
                        .into(holder.bookCover)
                }
            }

            holder.moreDetail.setOnClickListener{
                val initValues = InitFilterValuesReviewBookDetail()
                initValues.rating = reviewDetail!!.ratings
                initValues.text = reviewDetail.description
                initValues.reviewPhotos = reviewDetail.reviewPhotos

                context.let {
            //                val newTheme = R.style.BottomSheetSignNightTheme
            //                requireActivity().theme.applyStyle(newTheme, true)
                    ReviewDialogBookDetail(initValues).show(it){
                        style(SheetStyle.BOTTOM_SHEET)
                        onPositive {
                            this.dismiss()
                            if (bookID != null) {
                                updateRatingAndComment(bookID,getClientRating(), getClientReview(), getImage(), position)

                                notifyItemChanged(position)
                                val x = 1
                            } // Upload Comment
                        }
                    }
                }
            }
        }

    }

    private fun updateRatingAndComment(bookID: String, rating: Float, cmt: String, images: ArrayList<Photo>, position: Int) {
        val newItem = reviewList?.get(position)
        if (newItem != null) {
            // Re assign
            newItem.second.ratings = rating
            newItem.second.reviewPhotos = images
            newItem.second.description = cmt

            reviewList?.set(position, newItem) // Set back in the review list at changed data's position
        }


        val docBookRef = db.collection("books").whereEqualTo("bookID", bookID)

        val storageRef = storage.reference
        docBookRef.get().addOnSuccessListener { result ->
            for (document in result){
                val reviewsArray =
                    document.get("review") as ArrayList<Map<String, Any>>

                val uid = userInfo.userID

                // Each file added to storage
                for (image in images) {
                    val path = image.nameFile
                    val fileName = path?.substringAfterLast("/")
                    val reviewEachImageRef =
                        storageRef.child("reviewImages/${userInfo.userID}/${fileName}")

                    val baos = ByteArrayOutputStream()
                    image.image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                    val data = baos.toByteArray()
                    var uploadTask = reviewEachImageRef.putBytes(data)

                    uploadTask.addOnFailureListener { exception ->
                        Log.e("Upload Failure", "Failed to upload image: $exception")
                    }.addOnSuccessListener { taskSnapshot ->
                        Log.i("Upload Success", "Image uploaded successfully")
                        // You can also log or extract metadata if needed
                        val downloadUrl = taskSnapshot.storage.downloadUrl
                        Log.i("Download URL", "Download URL: $downloadUrl")
                    }

                }

                // update the path to user review uploaded image folder
                val folderImageRef = storageRef.child("reviewImages/${userInfo!!.userID}")
                val link = folderImageRef.toString()

                val updatedReview = mapOf(
                    "UID" to uid,
                    "image" to link,
                    "score" to rating.toInt(),
                    "text" to cmt
                )

                var reviewUpdated = false

                for ((index, review) in reviewsArray.withIndex()) {
                    if (review["UID"] == uid) { // Assuming "UID" uniquely identifies each review
                        reviewsArray[index] = updatedReview
                        reviewUpdated = true
                        break // Stop searching once the review is found and updated
                    }
                }

                // If the review is not found, add it to the front of the list
                if (!reviewUpdated) {
                    reviewsArray.add(0, updatedReview)
                }

                // Update the array field in the document
                document.reference.update("review", reviewsArray)
                    .addOnSuccessListener {
                        Log.i("update Review", "Success")
                    }
                    .addOnFailureListener {
                        Log.i("update Review", "Failed")
                    }

            }

        }

    }
}