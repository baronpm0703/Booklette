package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.booklette.model.BookObject
import com.squareup.picasso.Picasso

class HomeFragmentNewArrivaltemAdapter(private val context: Context, private val pageTitles: ArrayList<BookObject>,
                                       private val sales: ArrayList<Float>) : PagerAdapter() {

    @SuppressLint("SetTextI18n")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.home_fragment_new_arrival_item, container, false)

        val txtGenre = view.findViewById<TextView>(R.id.txtBookGenreNA)
        val txtName = view.findViewById<TextView>(R.id.txtBookNameNA)
        val txtAuthor = view.findViewById<TextView>(R.id.txtBookAuthorNA)
        val txtPrice = view.findViewById<TextView>(R.id.txtBookPriceNA)
        val txtSale = view.findViewById<TextView>(R.id.txtSaleNA)
        val ivBook = view.findViewById<ImageView>(R.id.ivBookNA)
        val rlSaleContainer = view.findViewById<RelativeLayout>(R.id.rlSaleNAContainer)


        txtGenre.text = pageTitles[position].genre
        txtName.text = pageTitles[position].name
        txtAuthor.text = pageTitles[position].author


        if (sales[position] != 0F)
        {
            val price = pageTitles[position].price
            txtPrice.text = (price - sales[position] * price).toString() + "VND"
            txtSale.text = (sales[position] * 100).toString() + "%"
        }
        else {
            val price = pageTitles[position].price
            txtPrice.text = (price - sales[position] * price).toString() + "VND"
            rlSaleContainer.visibility = View.INVISIBLE
            txtSale.visibility = View.INVISIBLE
        }

        Picasso.get()
            .load(pageTitles[position].image)
            .into(ivBook)

        container.addView(view)

        view.setOnClickListener {
            if (context is homeActivity) {
                val bdFragment = BookDetailFragment()

                val bundle = Bundle()
                bundle.putString("bookID", pageTitles[position].bookID)

                bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

                context.changeFragmentContainer(
                    bdFragment,
                    context.smoothBottomBarStack[context.smoothBottomBarStack.size - 1]
                )
            }
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return pageTitles.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}