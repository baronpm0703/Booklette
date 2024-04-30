package com.example.booklette

import ShipAddressObject
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R
import com.example.booklette.model.CartObject
import com.squareup.picasso.Picasso

class ShipAddressFragmentRecycleViewAdapter(
    private val context: Context,
    private var addressList: ArrayList<ShipAddressObject> = ArrayList(),


) : RecyclerView.Adapter<ShipAddressFragmentRecycleViewAdapter.ViewHolder>() {
    private var selectedAddressPosition = -1 // Initially no address selected
    private val addressSelections = HashMap<Int, Boolean>()

    interface OnEditAddressClickListener {
        fun onEditAddressClicked(address: ShipAddressObject)
    }

    private var editAddressClickListener: OnEditAddressClickListener? = null
    fun setOnEditAddressClickListener(listener: OnEditAddressClickListener) {
        editAddressClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buyerName: TextView = itemView.findViewById(R.id.buyerName)
        val editAddress: Button = itemView.findViewById(R.id.editAddress)
        val phoneNum: TextView = itemView.findViewById(R.id.phoneNum)
        val addressDetail: TextView = itemView.findViewById(R.id.addressDetail)
        val chooseAddressButton: RadioButton = itemView.findViewById(R.id.chooseAddressButton)
        val shipLabel: Button = itemView.findViewById(R.id.shipLabel)
        val addressZone: TextView = itemView.findViewById(R.id.addressZone)

    }

    interface OnAddressSelectedListener {
        fun onAddressSelected(address: ShipAddressObject)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.address_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val addressInfo = addressList[position]
        holder.buyerName.text = addressInfo.receiverName
        holder.phoneNum.text = addressInfo.receiverPhone
        holder.addressDetail.text = addressInfo.addressNumber
        holder.chooseAddressButton.isChecked = addressSelections.containsKey(position)
        holder.shipLabel.text = addressInfo.shipLabel
        holder.addressZone.text = addressInfo.ward  + ", "+ addressInfo.city + ", " + addressInfo.province


        if(addressInfo.shipLabel == "Work"){
            holder.shipLabel.setBackgroundResource(R.drawable.bank_card_gradient)
            holder.shipLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ship_address_work_label, 0, 0, 0);
        }
        else if(addressInfo.shipLabel == "Home"){
            holder.shipLabel.setBackgroundResource(R.drawable.bank_card_gradient_visa_card)
            holder.shipLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ship_address_home_label, 0, 0, 0);
        }

        // Set click listener to handle selection/deselection of the address
        holder.chooseAddressButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If this address is selected, clear all previous selections and mark this address as selected
                addressSelections.clear()
                addressSelections[position] = true
                addressInfo.isDefault = true
                notifyDataSetChanged() // Notify the adapter of the change to update UI
            } else {
                // If this address is deselected, remove it from the selections
                addressSelections.remove(position)
                addressInfo.isDefault = false

            }
        }
        holder.editAddress.setOnClickListener {
            editAddressClickListener?.onEditAddressClicked(addressInfo)
        }



    }




    override fun getItemCount(): Int {
        return addressList.size
    }

    fun removeItem(position: Int) {

    }

    fun getItemInfo(position: Int): ShipAddressObject {
        // Trả về thông tin của item tại vị trí được chỉ định
        return addressList[position]
    }

    fun getSelectedAddress(): ShipAddressObject? {
        // Find the selected address, if any
        val selectedPosition = addressSelections.keys.firstOrNull()
        return selectedPosition?.let { addressList[it] }
    }
}