package com.example.booklette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.BookObject
import com.example.booklette.model.BookObjectWithDate
import com.example.booklette.model.ProductsObject
import com.squareup.picasso.Picasso
import per.wsj.library.AndRatingBar

class TopBookHomeFragmentAdapter(
    val context: FragmentActivity?, private val dataList: ArrayList<BookObjectWithDate>,
    private val ratings: ArrayList<Float>) :
    RecyclerView.Adapter<TopBookHomeFragmentAdapter.ViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item_home_fragment, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        holder.txtName.text = dataList[position].name
        holder.txtAuthor.text = dataList[position].author
        holder.txtPrice.text = dataList[position].price.toString()

        Picasso.get()
            .load(dataList[position].image)
            .into(holder.ivTopBook)

        holder.topBookRating.rating = ratings[position]

        holder.itemView.setOnClickListener({
            var bdFragment = BookDetailFragment()

            var bundle = Bundle()
            bundle.putString("bookID", dataList[position].bookID)

            bdFragment.arguments = bundle
            if (context is homeActivity) {
                context.changeFragmentContainer(bdFragment, context.smoothBottomBarStack[context.smoothBottomBarStack.size - 1])
            }
        })

//        try {
//                holder.topBookRating.setRating(ratings[position])
//        }
//        catch (e: Exception) {
//
//        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size

    // Provide a reference to the views for each data item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtTopBookName)
        val txtAuthor = itemView.findViewById<TextView>(R.id.txtTopBookAuthor)
        val txtPrice = itemView.findViewById<TextView>(R.id.txtTopBookPrice)
        val ivTopBook = itemView.findViewById<ImageView>(R.id.ivTopBook)
        val topBookRating = itemView.findViewById<AndRatingBar>(R.id.topBookRating)
    }
}