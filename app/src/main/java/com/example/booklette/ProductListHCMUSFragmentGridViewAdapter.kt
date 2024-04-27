package com.example.booklette

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.booklette.model.ProductsObject
import com.squareup.picasso.Picasso

class ProductListHCMUSFragmentGridViewAdapter(
    private val context: Context,
    private var bookList: ArrayList<ProductsObject>,
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

    fun updateBookList(newBookList: ArrayList<ProductsObject>) {
        bookList = newBookList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            view = (inflater as LayoutInflater).inflate(R.layout.fragment_product_list_grid_item_for_hcmus, null)
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

        view.setOnClickListener {
            var bdFragment = BookDetailFragment()

            var bundle = Bundle()
            bundle.putString("bookID", bookInfo.bookID)
//                bundle.putFloat("price", pageTitles[position].price)
//                bundle.putFloat("salePercent", price_off[position])

            bdFragment.arguments = bundle

            val homeAct = (context as homeActivity)
            homeAct.changeFragmentContainer(bdFragment, context.smoothBottomBarStack[context.smoothBottomBarStack.size - 1]) //Let the homePage handle changing fragment
        }
        return view as View
    }

}