package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso

class ProductListFragmentGridViewAdapter(
    private val context: Context,
    private val bookList: ArrayList<ProductsObject>,
): BaseAdapter(){

    private class ViewHolder(row: View?) {
        var bookCover: ImageView? = null
        var bookRate: per.wsj.library.AndRatingBar? = null
        var bookTitle: TextView? = null
        var bookOwner: TextView? = null
        var bookPrice: TextView? = null

        init {
            bookCover = row?.findViewById(R.id.bookCover)
            bookRate = row?.findViewById(R.id.bookRate)
            bookTitle = row?.findViewById(R.id.bookTitle)
            bookOwner = row?.findViewById(R.id.bookOwner)
            bookPrice = row?.findViewById(R.id.bookPrice)
        }
    }
    override fun getCount(): Int {
        return bookList.size
    }

    override fun getItem(position: Int): Any {
        return bookList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            view = (inflater as LayoutInflater).inflate(R.layout.fragment_product_list_grid_item, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val bookInfo = bookList[position]

//        Log.i("GV", bookInfo.name)
        viewHolder.bookTitle?.text = bookInfo.name
        viewHolder.bookRate?.rating = bookInfo.rating
        viewHolder.bookOwner?.text = bookInfo.author
        val bookPrice = "${bookInfo.price} VND"
        viewHolder.bookPrice?.text = bookPrice
        Picasso.get()
            .load(bookInfo.image)
            .into(viewHolder.bookCover)


//        if (bookInfo.equals("2")) {
//            viewHolder.bookCover?.setImageResource(R.drawable.image_book_3)
//            viewHolder.bookTitle?.setText("The Catcher in the eyes")
//            viewHolder.bookRate?.rating = 4.0F
//            viewHolder.bookOwner?.setText("Phan Thai Khang")
//            viewHolder.bookPrice?.setText("150.000 VND")
//        } else if (bookInfo.equals("3")) {
//            viewHolder.bookCover?.setImageResource(R.drawable.image_book_4)
//            viewHolder.bookTitle?.setText("Tuesday with morie")
//            viewHolder.bookRate?.rating = 3.0F
//            viewHolder.bookOwner?.setText("Phan Thai Khang")
//            viewHolder.bookPrice?.setText("100.000 VND")
//        }

        return view as View
    }

}