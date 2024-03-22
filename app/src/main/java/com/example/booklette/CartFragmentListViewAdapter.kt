import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.example.booklette.R
import java.util.ArrayList

class CartFragmentListViewAdapter(
    private val context: Context,
    private val bookInCartList: ArrayList<String>
) : BaseAdapter() {

    override fun getCount(): Int {
        return bookInCartList.size
    }

    override fun getItem(position: Int): Any {
        return bookInCartList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun removeItem(position: Int) {
        bookInCartList.removeAt(position)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_cart_recycle_view_item, parent, false)
            viewHolder = ViewHolder(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val bookInfo = bookInCartList[position]

        if (bookInfo == "2") {
            viewHolder.bookCover.setImageResource(R.drawable.image_book_3)
            viewHolder.bookTitle.text = "The Catcher in the Rye"
            viewHolder.bookOwner.text = "Phan Thai Khang"
            viewHolder.bookPrice.text = "150.000 VND"
            viewHolder.btnSelectItem.isChecked = false

        } else if (bookInfo == "1") {
            viewHolder.bookCover.setImageResource(R.drawable.image_book_2)
            viewHolder.bookTitle.text = "The Secret Society of Aunts & Uncles"
            viewHolder.bookOwner.text = "Jake Gyllenhaal, Greta Caruso"
            viewHolder.bookPrice.text = "250.000 VND"
            viewHolder.btnSelectItem.isChecked = false

        }
        else if (bookInfo == "3") {
            viewHolder.bookCover.setImageResource(R.drawable.image_book_2)
            viewHolder.bookTitle.text = "The Secret Society of Aunts & Uncles"
            viewHolder.bookOwner.text = "Jake Gyllenhaal, Greta Caruso"
            viewHolder.bookPrice.text = "210.000 VND"
            viewHolder.btnSelectItem.isChecked = false

        }
        else if (bookInfo == "4") {
            viewHolder.bookCover.setImageResource(R.drawable.image_book_2)
            viewHolder.bookTitle.text = "HEHE Society of Aunts & Uncles"
            viewHolder.bookOwner.text = "Jake Gyllenhaal, Greta Caruso"
            viewHolder.bookPrice.text = "250.000 VND"
            viewHolder.btnSelectItem.isChecked = false

        }

        else if (bookInfo == "5") {
            viewHolder.bookCover.setImageResource(R.drawable.image_book_2)
            viewHolder.bookTitle.text = "HEHE Socixvcvcxvxcvxcvcxety of Aunts & Uncles"
            viewHolder.bookOwner.text = "Jake Gyllenhaal, Greta Caruso"
            viewHolder.bookPrice.text = "250.000 VND"
            viewHolder.btnSelectItem.isChecked = false

        }

        viewHolder.btnCartItemMinus.setOnClickListener {
            // Handle decreasing item quantity
            // You can call quantityChangeListener here if needed
            // Update tvCartItemCount
            var newCount = viewHolder.tvCartItemCount.text.toString().toInt() - 1
            if (newCount < 0) {
                newCount = 0
            }
            viewHolder.tvCartItemCount.text = newCount.toString()
        }

        viewHolder.btnCartItemAdd.setOnClickListener {
            // Handle increasing item quantity
            // You can call quantityChangeListener here if needed
            // Update tvCartItemCount
            var newCount = viewHolder.tvCartItemCount.text.toString().toInt() + 1
            viewHolder.tvCartItemCount.text = newCount.toString()
        }


        viewHolder.btnSelectItem.setOnClickListener {
            viewHolder.btnSelectItem.isChecked = !viewHolder.btnSelectItem.isChecked


        }

        return convertView!!
    }

    internal class ViewHolder(view: View) {
        val bookCover: ImageView = view.findViewById(R.id.bookCover)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle)
        val bookOwner: TextView = view.findViewById(R.id.bookOwner)
        val bookPrice: TextView = view.findViewById(R.id.bookPrice)
        val btnCartItemMinus: AppCompatButton = view.findViewById(R.id.btnCartItemMinus)
        val tvCartItemCount: AppCompatTextView = view.findViewById(R.id.tvCartItemCount)
        val btnCartItemAdd: AppCompatButton = view.findViewById(R.id.btnCartItemAdd)
        val btnSelectItem: RadioButton = view.findViewById(R.id.btnSelectItem)
    }
}
