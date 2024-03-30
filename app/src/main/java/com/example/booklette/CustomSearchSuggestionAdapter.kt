import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter

class CustomSuggestionAdapter(
    context: Context, inflater: LayoutInflater?,
    searchBar: MaterialSearchBar
) : SuggestionsAdapter<String, CustomSuggestionAdapter.SuggestionViewHolder>(inflater),
    Filterable {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalSuggestions = mutableListOf<String>()
    private var filteredSuggestions = mutableListOf<String>()
    private val maxSuggestionsToShow = 3 // Maximum number of suggestions to show
    private val searchbar = searchBar

    init {
        searchbar.lastSuggestions = originalSuggestions
    }

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
        holder.bind(suggestion, position)
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
        holder?.bind(suggestion!!, position)
    }

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(suggestion: String, position: Int) {
            textView.text = suggestion
            itemView.setOnClickListener {
                Log.i("Item Click", suggestion)
                // Handle click event here, for example:
                searchbar.hideSuggestionsList()
                searchbar.setText(suggestion)
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

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredSuggestions.clear()
                val resultsList = results?.values as List<String>
                filteredSuggestions.addAll(resultsList.take(resultsList.size))
                notifyDataSetChanged()
            }
        }
    }
}
