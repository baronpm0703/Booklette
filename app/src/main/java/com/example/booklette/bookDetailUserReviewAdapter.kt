package com.example.booklette

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.api.Context

class bookDetailUserReviewAdapter() :
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
//        holder.textView.text = dataList[position]
//        holder.itemView.setOnClickListener({
//            fragment.getRCDBookCategories(dataList[position].toString())
//        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = 2

    // Provide a reference to the views for each data item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val textView: TextView = itemView.findViewById(R.id.bookCategory)
    }
}