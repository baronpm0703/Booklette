package com.example.booklette

 import android.view.LayoutInflater
 import android.view.View
 import android.view.ViewGroup
 import android.widget.CheckBox
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

data class DetailBookItem(
    val ID: String, val name: String, val author: String, val amount: Long, val price: Float,
    val imageUrl: String, val bookStoreName: String)
class OrderDetailItemListRecyclerViewAdapter(
    private val values: List<DetailBookItem>,
    private var allowSelection: Boolean = false, // Default to not allow to select
    private var MultipleSelection: Boolean = false // Default to single selection
) : RecyclerView.Adapter<OrderDetailItemListRecyclerViewAdapter.ViewHolder>() {

    private val checkedBookIDList = mutableListOf<String>() // List of positions of checked items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentOrderDetailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }
    fun allowSelection(){
        allowSelection = true
    }
    fun allowMultiple(){
        MultipleSelection = true
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
//        holder.idView.text = item.id
//        holder.contentView.text = item.content

        holder.itemBookStore.text = item.bookStoreName
        holder.itemName.text = item.name
        holder.itemAuthor.text = item.author
        holder.itemAmount.text = item.amount.toString()
        holder.itemPrice.text = item.price.toString()
        if (item.imageUrl != "")
            Picasso.get().load(item.imageUrl).into(holder.itemImage)
        if (allowSelection){
            holder.itemCheckBox.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    if (!MultipleSelection) {
                        checkedBookIDList.clear() // Clear previous selection if single selection is enabled
                    }
                    checkedBookIDList.add(item.ID)
                } else {
                    checkedBookIDList.remove(item.ID)
                }


            }
        }
        else{
            holder.itemCheckBox.visibility = View.INVISIBLE
            holder.itemCheckBox.isClickable = false
        }

    }

    override fun getItemCount(): Int = values.size
    fun getCheckedItems(): List<String> {
        return checkedBookIDList
    }
    inner class ViewHolder(binding: FragmentOrderDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        val idView: TextView = binding.itemNumber
//        val contentView: TextView = binding.content

        val itemName: TextView = binding.orderDetailItemName
        val itemAuthor: TextView = binding.orderDetailItemAuthor
        val itemAmount: TextView = binding.orderDetailItemAmount
        val itemPrice: TextView = binding.orderDetailItemPrice
        val itemImage: ImageView = binding.orderDetailItemImage
        val itemBookStore: TextView = binding.orderDetailItemShopName
        val itemCheckBox: CheckBox = binding.orderDetailItemCheckBox
//        override fun toString(): String {
//            return super.toString() + " '" + contentView.text + "'"
//        }
    }

}