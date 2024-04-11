package com.example.booklette

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import per.wsj.library.AndRatingBar

class bookDetailUserReviewAdapter(var context: android.content.Context?, var datalist: ArrayList<UserReviewObject>) :
    RecyclerView.Adapter<bookDetailUserReviewAdapter.ViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_detail_user_review_item, parent, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        holder.txtUsername.text = datalist[position].userName
        holder.userReviewRatingBar.rating = datalist[position].ratings
        holder.txtUserReviewDescription.text = datalist[position].description

        Picasso.get()
            .load(datalist[position].avatar)
            .into(holder.imgAvatar)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = datalist.size

    // Provide a reference to the views for each data item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUsername: TextView = itemView.findViewById(R.id.userNameRating)
        val userReviewRatingBar: AndRatingBar = itemView.findViewById(R.id.userReviewRatingBar)
        val txtUserReviewDescription: TextView = itemView.findViewById(R.id.txtUserReviewDescription)
        val imgAvatar: CircleImageView = itemView.findViewById(R.id.imgUserRatingAvatar)
    }
}