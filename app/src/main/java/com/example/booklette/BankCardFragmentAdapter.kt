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
import com.maxpilotto.creditcardview.CreditCardView

class BankCardFragmentAdapter(
    private val context: Context,
    private val bankCardList: ArrayList<String>,

    ) : RecyclerView.Adapter<BankCardFragmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bankCardItem: CreditCardView = itemView.findViewById(R.id.bankCardItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.bank_card_recycler_view_layout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardInfor = bankCardList[position]
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