package com.example.booklette

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R

class ShipAddressFragmentRecycleViewAdapter(
    private val context: Context,
    private val addressList: ArrayList<String>,

    ) : RecyclerView.Adapter<ShipAddressFragmentRecycleViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buyerName: TextView = itemView.findViewById(R.id.buyerName)
        val editAddress: Button = itemView.findViewById(R.id.editAddress)
        val phoneNum: TextView = itemView.findViewById(R.id.phoneNum)
        val addressDetail: TextView = itemView.findViewById(R.id.addressDetail)
        val addressZone: TextView = itemView.findViewById(R.id.addressZone)
        val chooseAddressButton: RadioButton = itemView.findViewById(R.id.chooseAddressButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.address_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val addressInfo = addressList[position]


        if (addressInfo == "2") {
            holder.buyerName.text = "Vo Chanh Tin"
            holder.phoneNum.text = "0913794478"
            holder.addressDetail.text = "123 Tran Hung Dao"
            holder.addressZone.text = "Phuong 2, Quan 5"
        }
    }


    override fun getItemCount(): Int {
        return addressList.size
    }

    fun removeItem(position: Int) {

    }

    fun getItemInfo(position: Int): String {
        // Trả về thông tin của item tại vị trí được chỉ định
        return addressList[position]
    }
}