package com.example.booklette

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeFragmentTodayRCDTypeAdapter(private val dataList: ArrayList<String>) :
    RecyclerView.Adapter<HomeFragmentTodayRCDTypeAdapter.ViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.today_recommandation_home_fragment_choice_list_view, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        holder.textView.text = dataList[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size

    // Provide a reference to the views for each data item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.bookCategory)
    }
}