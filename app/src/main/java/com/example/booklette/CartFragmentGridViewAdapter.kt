import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.booklette.R

class CartFragmentGridViewAdapter(
    // on below line we are creating two
    // variables for course list and context
    private val context: Context,
    private val bookInCartList: ArrayList<String>
): BaseAdapter(){

    private class ViewHolder(row: View?) {
        var bookCover: ImageView? = null
        var bookTitle: TextView? = null
        var bookOwner: TextView? = null
        var bookPrice: TextView? = null

        init {
            bookCover = row?.findViewById(R.id.bookCover)
            bookTitle = row?.findViewById(R.id.bookTitle)
            bookOwner = row?.findViewById(R.id.bookOwner)
            bookPrice = row?.findViewById(R.id.bookPrice)
        }
    }
    override fun getCount(): Int {
        return bookInCartList.size
    }

    override fun getItem(position: Int): Any {
        return bookInCartList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            view = (inflater as LayoutInflater).inflate(R.layout.fragment_cart_grid_item, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var bookInfo = bookInCartList[position]
        if (bookInfo.equals("2")) {
            viewHolder.bookCover?.setImageResource(R.drawable.image_book_3)
            viewHolder.bookTitle?.setText("The Catcher in the eyes")
            viewHolder.bookOwner?.setText("Phan Thai Khang")
            viewHolder.bookPrice?.setText("150.000 VND")
        }
        if (bookInfo.equals("1")) {
            viewHolder.bookCover?.setImageResource(R.drawable.image_book_2)
            viewHolder.bookTitle?.setText("The Secret Society of Aunts & Uncles")
            viewHolder.bookOwner?.setText("Jake Gyllenhaal, Greta Caruso,")
            viewHolder.bookPrice?.setText("250.000 VND")
        }

        return view as View
    }

}