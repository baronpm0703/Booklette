package com.example.booklette
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import android.widget.Filter
import android.widget.Filterable

class CustomSuggestionAdapter(
    context: Context, inflater: LayoutInflater?,
) : SuggestionsAdapter<String, CustomSuggestionAdapter.SuggestionViewHolder>(inflater) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalSuggestions = mutableListOf<String>()
    private var filteredSuggestions = mutableListOf<String>()
    private val maxSuggestionsToShow = 3// Maximum number of suggestions to show

    @SuppressLint("NotifyDataSetChanged")
    override fun setSuggestions(newSuggestions: List<String>) {
        originalSuggestions.clear()
        originalSuggestions.addAll(newSuggestions)
        filteredSuggestions.clear()
        filteredSuggestions.addAll(newSuggestions.take(maxSuggestionsToShow))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = filteredSuggestions[position]
        holder.bind(suggestion)
    }

    override fun getItemCount(): Int {
        return filteredSuggestions.size
    }

    override fun getSingleViewHeight(): Int {
        return 50
    }

    override fun onBindSuggestionHolder(
        suggestion: String?,
        holder: SuggestionViewHolder?,
        position: Int
    ) {
        holder?.bind(suggestion!!)
    }

    fun getView(context: Context?, position: Int, convertView: View?, parent: ViewGroup?): View {
        return convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
    }

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(suggestion: String) {
            textView.text = suggestion
            itemView.setOnClickListener {
                Log.i("Item Click", suggestion)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<String>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(originalSuggestions.take(maxSuggestionsToShow))
                } else {
                    val searchText = constraint.toString().toLowerCase().trim()
                    originalSuggestions.forEach { suggestion ->
                        if (suggestion.toLowerCase().contains(searchText)) {
                            filteredList.add(suggestion)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredSuggestions.clear()
                val resultsList = results?.values as List<String>
                maxSuggestionsCount = resultsList.size
                filteredSuggestions.addAll(resultsList.take(resultsList.size))
                Log.i("Filter sIZE", this@CustomSuggestionAdapter.itemCount.toString())
                notifyDataSetChanged()
            }
        }
    }
}
