package com.example.booklette


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CheckOutListViewAdapter(context: Context, private val data: List<CheckoutItem>) :
    ArrayAdapter<CheckoutItem>(context, R.layout.fragment_check_out_list_view_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val viewHolder: ViewHolder
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.fragment_check_out_list_view_item, parent, false)
            viewHolder = ViewHolder(itemView)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        val currentItem = data[position]

        viewHolder.shopNameTextView.text = currentItem.shopName
        viewHolder.bookTitleTextView.text = currentItem.bookTitle
        viewHolder.bookOwnerTextView.text = currentItem.bookOwner
        viewHolder.bookPriceTextView.text = currentItem.bookPrice
        viewHolder.quantityTextView.text = currentItem.tvCartItemCount

        // Set other views accordingly

        return itemView!!
    }

    private class ViewHolder(view: View) {
        val shopNameTextView: TextView = view.findViewById(R.id.shopName)
        val bookTitleTextView: TextView = view.findViewById(R.id.bookTitle)
        val bookOwnerTextView: TextView = view.findViewById(R.id.bookOwner)
        val bookPriceTextView: TextView = view.findViewById(R.id.bookPrice)
        val quantityTextView: TextView = view.findViewById(R.id.tvCartItemCount)

        // Initialize other views here
    }
}