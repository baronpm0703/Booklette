package com.example.booklette

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.BookObject
import com.example.booklette.model.Photo
import com.example.booklette.model.UserReviewObject
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await

class MyBookReviewListRVAdapter(
    private val context: Context,
    private var reviewList: ArrayList<Pair<String, UserReviewObject>>?
): RecyclerView.Adapter<MyBookReviewListRVAdapter.ViewHolder>() {
    private lateinit var db: FirebaseFirestore
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

        return ViewHolder(wishlistView)
    }

    override fun getItemCount(): Int {
        return reviewList?.size ?: 0
    }

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

                holder.review?.text = descriptions

                if (image.isNotEmpty()) {
                    Picasso.get()
                        .load(image)
                        .into(holder.bookCover)
                }
            }

            holder.moreDetail.setOnClickListener{

            }
        }

    }
}