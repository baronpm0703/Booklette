package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ProductListFragmentGridViewAdapter(
    private val context: Context,
    private val bookList: ArrayList<String>
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

        var bookInfo = bookList[position]
        if (bookInfo.equals("2")) {
            viewHolder.bookCover?.setImageResource(R.drawable.image_book_3)
            viewHolder.bookTitle?.setText("The Catcher in the eyes")
            viewHolder.bookRate?.rating = 4.0F
            viewHolder.bookOwner?.setText("Phan Thai Khang")
            viewHolder.bookPrice?.setText("150.000 VND")
        }

        return view as View
    }

}