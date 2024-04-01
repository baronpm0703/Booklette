package com.example.booklette

import android.annotation.SuppressLint
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import per.wsj.library.AndRatingBar

class MyShopHighlyRecommendedAdapter(private val books : List<HRecommendedBookObject>) :
	RecyclerView.Adapter<MyShopHighlyRecommendedAdapter.ViewHolder>() {

	private lateinit var mListerner: onItemClickListener

	interface onItemClickListener {
		fun onItemClick(position: Int)
	}

	fun setOnItemClickListener(clickListener: onItemClickListener) {
		mListerner = clickListener
	}

	class ViewHolder(listItemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(listItemView) {
		val bookImg = listItemView.findViewById(R.id.bookImg) as ImageView
		val bookRating = listItemView.findViewById(R.id.bookRating) as AndRatingBar
		val bookNameText = listItemView.findViewById(R.id.bookNameText) as TextView
		val bookPriceText = listItemView.findViewById(R.id.bookPriceText) as TextView

		init {
			itemView.setOnClickListener {
				clickListener.onItemClick(absoluteAdapterPosition)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val context = parent.context
		val inflater = LayoutInflater.from(context)
		// Inflate the custom layout
		val bookView = inflater.inflate(R.layout.myshop_highly_recommended_book_item, parent, false)
		// Return a new holder instance
		return ViewHolder(bookView, mListerner)
	}

	override fun getItemCount(): Int {
		return books.size
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		// Get the data model based on position
		val book: HRecommendedBookObject = books[position]
		// Set item views based on your views and data model
		Picasso.get()
			.load(book.basicInfo.image)
			.into(holder.bookImg)
		holder.bookRating.rating = book.rating
		holder.bookNameText.text = book.basicInfo.name
		holder.bookPriceText.text = book.basicInfo.price.toString()
	}
}