package com.example.booklette

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.MyOrderItemBinding
import com.example.booklette.placeholder.PlaceholderContent.PlaceholderItem
import java.util.Date

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
data class OrderDataClass(
    val ID: String,
    val creationDate: Date, val trackingNumber: String, val quantity: Long, val total: Float, val status: String)
class MyOrderItemRecyclerViewAdapter(
    private var values: List<OrderDataClass>
) : RecyclerView.Adapter<MyOrderItemRecyclerViewAdapter.ViewHolder>() {
    var onButtonClick: ((OrderDataClass) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            MyOrderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        holder.orderNumber.text  = item.ID
        holder.dateOrder.text = sdf.format(item.creationDate)
        holder.trackingNumber.text = "  " + item.trackingNumber

        holder.quantityLabel.text = "  " + item.quantity.toString()
        holder.totalLabel.text = "  " + item.total.toString()
        holder.statusField.text = item.status

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: MyOrderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val orderNumber: TextView = binding.orderNumberField
        val dateOrder: TextView = binding.dateOrder
        val trackingNumber: TextView = binding.trackingNumberField
        val quantityLabel: TextView = binding.quantityField
        val totalLabel: TextView = binding.totalField
        val statusField: TextView = binding.statusField
        val detailButton: Button = binding.detailButton
//        override fun toString(): String {
//            //return super.toString() + " '" + contentView.text + "'"
//        }
        init {
            detailButton.setOnClickListener {
                val position = bindingAdapterPosition // or absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onButtonClick?.invoke(values[position])
                }
            }
        }

    }

}