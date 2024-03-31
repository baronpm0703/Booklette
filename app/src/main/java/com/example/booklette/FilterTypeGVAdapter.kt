package com.example.booklette

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class FilterTypeGVAdapter(
    private val context: Context,
    private val typeList: ArrayList<String>
): BaseAdapter(){
    private class ViewHolder(row: View?) {
        var item: com.google.android.material.chip.Chip? = null
        init {
            item = row?.findViewById(R.id.filterCategory)
        }
    }
    private var checkedItems = BooleanArray(this.count)
    init {
        checkedItems.fill(false)
    }

    override fun getCount(): Int {
        return typeList.size
    }

    override fun getItem(position: Int): Any {
        return typeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            view = (inflater as LayoutInflater).inflate(R.layout.filter_category_gv_items, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var typeInfo = typeList[position]
        viewHolder.item?.text = typeInfo
        viewHolder.item?.isChecked = checkedItems[position]

        viewHolder.item?.setOnCheckedChangeListener { _, isChecked ->
            checkedItems[position] = isChecked

            (parent as? AdapterView<*>?)?.performItemClick(view, position, getItemId(position))
        }

        return view as View
    }
    fun setCheckedItems(checkedItems: BooleanArray) {
        if (checkedItems.size != this.checkedItems.size) {
            throw IllegalArgumentException("Size of checkedItems array must match the size of chipNames array")
        }
        this.checkedItems.indices.forEach { index ->
            this.checkedItems[index] = checkedItems[index]
        }
        notifyDataSetChanged()
    }
}