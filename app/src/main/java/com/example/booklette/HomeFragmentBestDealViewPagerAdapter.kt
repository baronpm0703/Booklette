package com.example.booklette

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Picasso

class HomeFragmentBestDealViewPagerAdapter(private val context: Context, private val pageTitles: ArrayList<BookObject>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.best_deal_recycler_view_layout, container, false)

        val txtName = view.findViewById<TextView>(R.id.txtBestDealRVNameItem)
        val txtAuthor = view.findViewById<TextView>(R.id.txtBestDealRVAuthorItem)
        val ivBook = view.findViewById<ImageView>(R.id.ivBookBestDealItem)
        val txtCategory = view.findViewById<TextView>(R.id.txtBestDealRVCategoryItem)
        val txtPrice = view.findViewById<TextView>(R.id.txtBestDealRVPriceItem)

        txtName.text = pageTitles[position].name
        txtAuthor.text = pageTitles[position].author
        txtCategory.text = pageTitles[position].genre
        txtPrice.text = pageTitles[position].price.toString()
        Picasso.get()
            .load(pageTitles[position].image)
            .into(ivBook)

        container.addView(view)

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