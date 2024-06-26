package com.example.booklette

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.booklette.model.BookObject
import com.squareup.picasso.Picasso

class HomeFragmentBestDealViewPagerAdapter(private val context: Context, private val pageTitles: ArrayList<BookObject>,
                                           private val price_off: ArrayList<Float>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.best_deal_recycler_view_layout, container, false)

        val txtName = view.findViewById<TextView>(R.id.txtBestDealRVNameItem)
        val txtAuthor = view.findViewById<TextView>(R.id.txtBestDealRVAuthorItem)
        val ivBook = view.findViewById<ImageView>(R.id.ivBookBestDealItem)
        val txtCategory = view.findViewById<TextView>(R.id.txtBestDealRVCategoryItem)
        val txtPrice = view.findViewById<TextView>(R.id.txtBestDealRVPriceItem)
        val txtPriceOff = view.findViewById<TextView>(R.id.txtBestDealRVPriceOffItem)

        txtName.text = pageTitles[position].name
        txtAuthor.text = pageTitles[position].author
        txtCategory.text = pageTitles[position].genre
        txtPriceOff.text = "%.0f".format(price_off[position] * 100) + "% OFF"
        val price = pageTitles[position].price
        txtPrice.text = (price - price * price_off[position]).toString()
        Picasso.get()
            .load(pageTitles[position].image)
            .into(ivBook)

        container.addView(view)

        view.setOnClickListener({
            if (context is homeActivity) {
                var bdFragment = BookDetailFragment()

                var bundle = Bundle()
                bundle.putString("bookID", pageTitles[position].bookID)
//                bundle.putFloat("price", pageTitles[position].price)
//                bundle.putFloat("salePercent", price_off[position])

                bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

                context.changeFragmentContainer(bdFragment, context.smoothBottomBarStack[context.smoothBottomBarStack.size - 1])
            }
        })

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