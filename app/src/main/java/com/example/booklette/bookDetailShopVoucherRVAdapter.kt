package com.example.booklette

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.VoucherObject
import java.time.Instant

class bookDetailShopVoucherRVAdapter(var context: android.content.Context?, var data: ArrayList<VoucherObject>) :
    RecyclerView.Adapter<bookDetailShopVoucherRVAdapter.ViewHolder>() {

    interface VoucherItemClickListener {
        fun onVoucherItemClick(percentage: Float)
    }

    var itemClickListener: VoucherItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(percentage: Float)
    }
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shop_voucher_book_detail_item, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view with that element
//        holder.textView.text = dataList[position]
//        holder.itemView.setOnClickListener({
//            fragment.getRCDBookCategories(dataList[position].toString())
//        })

        holder.txtVoucherCode.text = data[position].discountID
        holder.txtDesciptionVoucher.text = data[position].discountName
        holder.txtVoucherRemainingTime.text = calculateRemainingDays(Instant.now().epochSecond, data[position].endDate.seconds).toString() + " day(s) left"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = data.size

    // Provide a reference to the views for each data item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVoucherCode: TextView = itemView.findViewById(R.id.txtVoucherCode)
        val txtDesciptionVoucher: TextView = itemView.findViewById(R.id.txtDesciptionVoucher)
        val txtVoucherRemainingTime: TextView = itemView.findViewById(R.id.txtVoucherRemainingTime)
        val btnSaveVoucher: Button = itemView.findViewById(R.id.btnSaveVoucher)

        init {
            btnSaveVoucher.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val voucher = data[position]
                    val percentage = voucher.percent
                    itemClickListener?.onVoucherItemClick(percentage)
                }
            }
        }
    }


    private fun calculateRemainingDays(currentTimestamp: Long, variableUnixTimestamp: Long): Long {
        val differenceSeconds = variableUnixTimestamp - currentTimestamp
        return differenceSeconds / (24 * 3600)
    }
}