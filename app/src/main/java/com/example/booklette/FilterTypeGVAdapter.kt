package com.example.booklette

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class FilterTypeGVAdapter(
    private val context: Context,
    private val typeList: ArrayList<String>
): BaseAdapter(){
    private class ViewHolder(row: View?) {
        var item: TextView? = null

        init {
            item = row?.findViewById(R.id.filterCategory)
        }
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

        return view as View
    }
}