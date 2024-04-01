package com.example.booklette

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R
import com.maxpilotto.creditcardview.CreditCardView

class BankCardFragmentAdapter(
    private val context: Context,
    private val bankCardList: ArrayList<String>,

    ) : RecyclerView.Adapter<BankCardFragmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardType: TextView = itemView.findViewById(R.id.cardType)
        val cardNumber: TextView = itemView.findViewById(R.id.cardNumber)
        val cardHolder: TextView = itemView.findViewById(R.id.cardHolder)
        val expireDate: TextView = itemView.findViewById(R.id.expireDate)
        val cardBackground: LinearLayout = itemView.findViewById(R.id.cardBackground)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.bank_card_recycler_view_layout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardInfor = bankCardList[position]
        if(cardInfor == "1"){
            holder.cardHolder.text = "Phan Thai Khang"
            holder.expireDate.text = "05/2028"
            holder.cardBackground.setBackgroundResource(R.drawable.bank_card_gradient_visa_card)
        }
        if(cardInfor == "2"){
            holder.cardHolder.text = "Vo Chanh Tin"
            holder.expireDate.text = "08/2028"
            holder.cardBackground.setBackgroundResource(R.drawable.bank_card_gradient)
        }
    }


    override fun getItemCount(): Int {
        return bankCardList.size
    }

    fun removeItem(position: Int) {

    }
    fun getItemInfo(position: Int): String {
        // Trả về thông tin của item tại vị trí được chỉ định
        return bankCardList[position]
    }
}