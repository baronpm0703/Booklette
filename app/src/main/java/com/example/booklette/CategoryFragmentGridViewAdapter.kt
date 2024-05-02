import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.ProductList
import com.example.booklette.R
import com.example.booklette.homeActivity

class CategoryFragmentGridViewAdapter(
    private val context: Context,
    private val courseList: ArrayList<String>
) : RecyclerView.Adapter<CategoryFragmentGridViewAdapter.ViewHolder>() {

    // ViewHolder class to hold references to the views
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseTV: TextView = itemView.findViewById(R.id.txtCategoryNameItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_category_grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courseList[position]
        holder.courseTV.text = course

        holder.itemView.setOnClickListener({
            val genre = courseList[position]
            val productList = ProductList()
            val args = Bundle()
            args.putString("Genre", genre)
            productList.arguments = args

            val homeAct = (context as homeActivity)
            homeAct.changeFragmentContainer(productList, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        })
    }

    override fun getItemCount(): Int {
        return courseList.size
    }
}

//class CategoryFragmentGridViewAdapter(
//    // on below line we are creating two
//    // variables for course list and context
//    private val context: Context,
//    private val courseList: ArrayList<String>
//) :
//    BaseAdapter() {
//    // in base adapter class we are creating variables
//    // for layout inflater, course image view and course text view.
//    private var layoutInflater: LayoutInflater? = null
//    private lateinit var courseTV: TextView
//
//    // below method is use to return the count of course list
//    override fun getCount(): Int {
//        return courseList.size
//    }
//
//    // below function is use to return the item of grid view.
//    override fun getItem(position: Int): Any? {
//        return null
//    }
//
//    // below function is use to return item id of grid view.
//    override fun getItemId(position: Int): Long {
//        return 0
//    }
//
//    // in below function we are getting individual item of grid view.
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
//        var convertView = convertView
//        // on blow line we are checking if layout inflater
//        // is null, if it is null we are initializing it.
//        if (layoutInflater == null) {
//            layoutInflater =
//                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        }
//        // on the below line we are checking if convert view is null.
//        // If it is null we are initializing it.
//        if (convertView == null) {
//            // on below line we are passing the layout file
//            // which we have to inflate for each item of grid view.
//            convertView = layoutInflater!!.inflate(R.layout.fragment_category_grid_item, null)
//        }
//        // on below line we are initializing our course image view
//        // and course text view with their ids.
//
//        courseTV = convertView!!.findViewById(R.id.txtCategoryNameItem)
//
//        // on below line we are setting text in our course text view.
//        courseTV.setText(courseList[position])
//        // at last we are returning our convert view.
//        return convertView
//    }
//}