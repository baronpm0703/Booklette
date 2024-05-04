package com.example.booklette

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.BookObject
import com.example.booklette.model.Photo
import com.squareup.picasso.Picasso

class UserFavoritesBookRVAdapter(
    private val context: Context,
    private var wishList: ArrayList<BookObject>?
): RecyclerView.Adapter<UserFavoritesBookRVAdapter.ViewHolder>() {

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val bookCover = listItemView.findViewById<ImageView>(R.id.favoritesBookCover)
        val genre = listItemView.findViewById<TextView>(R.id.favoritesBookGenre)
        val bookName = listItemView.findViewById<TextView>(R.id.favoritesBookName)
        val owner = listItemView.findViewById<TextView>(R.id.favoritesBookOwner)
        val price = listItemView.findViewById<TextView>(R.id.favoritesBookPrice)

    }


    fun updateWishList(wishList: ArrayList<BookObject>){
        this.wishList = wishList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val wishlistView = inflater.inflate(R.layout.fragment_wishlist_recycle_view_item, parent, false)

        return ViewHolder(wishlistView)
    }

    override fun getItemCount(): Int {
        return wishList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val detail: BookObject? = wishList?.get(position)

//        Log.i("GV", bookInfo.name)
        if (detail != null) {
            holder.genre?.text = detail.genre
            holder.bookName?.text = detail.name
            holder.owner?.text = detail.author
            val bookPrice = "${detail.price} VND"
            holder.price?.text = bookPrice

            if (detail.image.isNotEmpty()) {
                Picasso.get()
                    .load(detail.image)
                    .into(holder.bookCover)
            }
        }

    }

    fun removeAt(index: Int) {
        wishList?.removeAt(index)   // items is a MutableList
        notifyItemRemoved(index)
    }
}