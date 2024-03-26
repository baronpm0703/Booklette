package com.example.booklette

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class FilterAgeGVAdapter(
    private val context: Context,
    private val ageList: ArrayList<String>
): BaseAdapter(){
    private class ViewHolder(row: View?) {
        var item: TextView? = null

        init {
            item = row?.findViewById(R.id.filterCategory)
        }
    }

    override fun getCount(): Int {
        return ageList.size
    }

    override fun getItem(position: Int): Any {
        return ageList[position]
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

        var ageInfo = ageList[position]
        viewHolder.item?.text = ageInfo

        return view as View
    }
}