package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.AddBookToShopDialogBinding
import com.example.booklette.databinding.ManageshopDiscountprogramsAddChooseproductDialogBinding
import com.example.booklette.model.Photo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso

class ProductListAdapter(private val books : List<Map<String, String>>) :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {

    private lateinit var mListerner: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener) {
        mListerner = clickListener
    }

    class ViewHolder(listItemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(listItemView) {
        val bookImage = listItemView.findViewById(R.id.bookImage) as ImageView
        val bookName = listItemView.findViewById(R.id.bookName) as TextView

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.manageshop_discountprograms_add_chooseproduct_item, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView, mListerner)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val book: Map<String, String> = books[position]
        // Set item views based on your views and data model
        Picasso.get().load(book["image"]).into(holder.bookImage)
        holder.bookName.text = book["name"]
    }
}

class ManageShopDiscountProgramsAddChooseProductDialog(storeRef: DocumentReference): Sheet() {
    override val dialogTag = "AddBook"

    private lateinit var binding: ManageshopDiscountprogramsAddChooseproductDialogBinding
    val storeRef = storeRef

    private var productImage = ""
    private var productID = ""

    fun getProductImage(): String {
        return productImage
    }

    fun getProductID(): String {
        return productID
    }

    fun onPositive(positiveListener: PositiveListener) {
        this.positiveListener = positiveListener
    }

    fun onPositive(@StringRes positiveRes: Int, positiveListener: PositiveListener? = null) {
        this.positiveText = windowContext.getString(positiveRes)
        this.positiveListener = positiveListener
    }

    fun onPositive(positiveText: String, positiveListener: PositiveListener? = null) {
        this.positiveText = positiveText
        this.positiveListener = positiveListener
    }

    override fun onCreateLayoutView(): View {
        return ManageshopDiscountprogramsAddChooseproductDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        val bookColl = db.collection("books")
        storeRef.get().addOnSuccessListener {
            val books = arrayListOf<Map<String, String>>()

            val items = it.get("items") as Map<String, Map<String, Any>>
            items.keys.forEach { key ->
                bookColl.whereEqualTo("bookID", key).get().addOnSuccessListener {snapshot ->
                    for (bookInfo in snapshot.documents) {
                        val book = hashMapOf<String, String>()
                        bookInfo.getString("bookID")?.let { it1 -> book.put("id", it1) }
                        bookInfo.getString("image")?.let { it1 -> book.put("image", it1) }
                        bookInfo.getString("name")?.let { it1 -> book.put("name", it1) }
                        books.add(book)
                    }

                    Handler().postDelayed({
                        if (items.keys.indexOf(key) == items.size - 1) {
                            var adapter = ProductListAdapter(books)
                            binding.productsRV.adapter = adapter
                            binding.productsRV.layoutManager = GridLayoutManager(context, 2)
                            adapter.setOnItemClickListener(object: ProductListAdapter.onItemClickListener {
                                override fun onItemClick(position: Int) {
                                    productImage = books[position]["image"].toString()
                                    productID = books[position]["id"].toString()

                                    positiveListener?.invoke()
                                }
                            })
                        }
                    }, 10)
                }
            }
        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    fun build(ctx: Context, width: Int? = null, func: ManageShopDiscountProgramsAddChooseProductDialog.() -> Unit): ManageShopDiscountProgramsAddChooseProductDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ManageShopDiscountProgramsAddChooseProductDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ManageShopDiscountProgramsAddChooseProductDialog.() -> Unit): ManageShopDiscountProgramsAddChooseProductDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}