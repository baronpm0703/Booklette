package com.example.booklette

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentOrderDetailItemBinding
import com.example.booklette.placeholder.PlaceholderContent.PlaceholderItem
import com.squareup.picasso.Picasso


/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */

data class EinvoiceBookItem(
    val ID: String, val name: String, val author: String, val amount: Long, val price: Float,
    val imageUrl: String)
class EinvoiceDetailItemListRecyclerViewAdapter(
    private val values: List<DetailBookItem>
) : RecyclerView.Adapter<EinvoiceDetailItemListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentOrderDetailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.itemAmount.text = item.amount.toString()
        holder.itemPrice.text = item.price.toString()

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentOrderDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        val idView: TextView = binding.itemNumber
//        val contentView: TextView = binding.content
        val itemName: TextView = binding.orderDetailItemName
        val itemAmount: TextView = binding.orderDetailItemAmount
        val itemPrice: TextView = binding.orderDetailItemPrice
    }

}