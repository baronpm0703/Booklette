package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.AddBookToShopDialogBinding
import com.example.booklette.model.Photo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle
import java.sql.Date


class ManageShopAddBookToShopDialog(
    private var initValue: ManageShopAddBookToShopDialogInitFilterValues
): Sheet() {
    override val dialogTag = "AddBook"

    private lateinit var binding: AddBookToShopDialogBinding
    private var bookName: String = ""
    private var bookAuthor: String = ""
    private var bookCategory: String = ""
    private var bookType: String = ""
    private var bookDesc: String = ""
    private var bookPrice: String = ""
    private var bookQuantity: String = ""
    private var dataPhoto : ArrayList<Photo> = arrayListOf()

    fun getBookName(): String {
        return bookName
    }
    fun getBookAuthor(): String {
        return bookAuthor
    }
    fun getBookCategory(): String {
        return bookCategory
    }
    fun getBookType(): String {
        return bookType
    }
    fun getBookDesc(): String {
        return bookDesc
    }
    fun getBookPrice(): String {
        return bookPrice
    }
    fun getBookQuantity(): String {
        return bookQuantity
    }

    fun getImage(): ArrayList<Photo> {
        return dataPhoto
    }

    fun getBookObject(): HashMap<String, Any> {
        return hashMapOf(
            "name" to bookName,
            "author" to bookAuthor,
            "genre" to bookCategory,
            "releaseDate" to Timestamp(Date.valueOf("2000-01-01")),
            "type" to bookType,
            "desc" to bookDesc,
            "price" to bookPrice,
            "quantity" to bookQuantity
        )
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

    fun updateinitValue(initValue: ManageShopAddBookToShopDialogInitFilterValues){
        this.initValue = initValue
    }

    override fun onCreateLayoutView(): View {
        return AddBookToShopDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore

        bookName = initValue.name
        binding.bookNameET.setText(bookName)
        bookAuthor = initValue.author
        binding.bookAuthorET.setText(bookAuthor)
        bookDesc = initValue.desc
        binding.bookDescET.setText(bookDesc)
        bookPrice = initValue.price
        binding.bookPriceET.setText(bookPrice)
        bookQuantity = initValue.quantity
        binding.bookQuantityET.setText(bookQuantity)
        bookCategory = initValue.category
        bookType = initValue.type

        // Book category spinner
        db.collection("book-category").document("IEBLyJGkGNq4ewmONSTd").get().addOnSuccessListener {
            val categories = it.get("categories") as ArrayList<String>
            categories.add(0, resources.getString(R.string.manageshop_mybooks_add_category))

            val bookCategorySpinner = binding.bookCategorySpin
            val bookCategoryAdapter = object: ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, categories) {
                override fun isEnabled(position: Int): Boolean {
                    return position != 0
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    if (position == 0) (view as TextView).setTextColor(Color.GRAY)
                    else (view as TextView).setTextColor(Color.BLACK)

                    view.setTypeface(Typeface.DEFAULT_BOLD)
                    return view
                }
            }
            bookCategorySpinner.adapter = bookCategoryAdapter
            bookCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View?, position: Int, id: Long) {
                    if (view == null) return

                    if (position == 0) (view as TextView).setTextColor(Color.GRAY)
                    else (view as TextView).setTextColor(Color.BLACK)

                    view.setTypeface(Typeface.DEFAULT_BOLD)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
        // Book type spinner
        val bookTypeItems = resources.getStringArray(R.array.manageshop_mybooks_add_type)
        val bookTypeSpinner = binding.bookTypeSpin
        val bookTypeAdapter = object: ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, bookTypeItems) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (position == 0) (view as TextView).setTextColor(Color.GRAY)
                else (view as TextView).setTextColor(Color.BLACK)

                view.setTypeface(Typeface.DEFAULT_BOLD)
                return view
            }
        }
        bookTypeSpinner.adapter = bookTypeAdapter
        bookTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                if (view == null) return

                if (position == 0) (view as TextView).setTextColor(Color.GRAY)
                else (view as TextView).setTextColor(Color.BLACK)

                view.setTypeface(Typeface.DEFAULT_BOLD)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        // Click out editext
        binding.addBookDialog.setOnClickListener {
            binding.bookNameET.clearFocus()
            binding.bookAuthorET.clearFocus()
            binding.bookCategorySpin.clearFocus()
            binding.bookTypeSpin.clearFocus()
            binding.bookDescET.clearFocus()
            binding.bookPriceET.clearFocus()
            binding.bookQuantityET.clearFocus()
        }
        binding.bookNameET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                bookName = binding.bookNameET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bookNameET.windowToken, 0)
            }
        }
        binding.bookAuthorET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                bookAuthor = binding.bookAuthorET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bookAuthorET.windowToken, 0)
            }
        }
//        binding.bookCategoryET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
//            // If EditText loses focus
//            if (!hasFocus) {
//                bookCategory = binding.bookCategoryET.text.toString()
//                // Hide the keyboard if it's currently showing
//                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.hideSoftInputFromWindow(binding.bookCategoryET.windowToken, 0)
//            }
//        }
//        binding.bookTypeET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
//            // If EditText loses focus
//            if (!hasFocus) {
//                bookType = binding.bookTypeET.selectedItem.toString()
//                // Hide the keyboard if it's currently showing
//                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.hideSoftInputFromWindow(binding.bookTypeET.windowToken, 0)
//            }
//        }
        binding.bookDescET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                bookDesc = binding.bookDescET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bookDescET.windowToken, 0)
            }
        }
        binding.bookPriceET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                bookPrice = binding.bookPriceET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bookPriceET.windowToken, 0)
            }
        }
        binding.bookQuantityET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                bookQuantity = binding.bookQuantityET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bookQuantityET.windowToken, 0)
            }
        }

        //Apply
        binding.addBtn.setOnClickListener {
            bookName = binding.bookNameET.text.toString()
            bookAuthor = binding.bookAuthorET.text.toString()
            bookCategory = binding.bookCategorySpin.selectedItem.toString()
            bookType = binding.bookTypeSpin.selectedItem.toString()
            bookDesc = binding.bookDescET.text.toString()
            bookPrice = binding.bookPriceET.text.toString()
            bookQuantity = binding.bookQuantityET.text.toString()
            positiveListener?.invoke()
        }

        binding.addPhotoLn.setOnClickListener {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(galleryIntent, 2222)
        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            2222 -> if (null != data) {
                val imageUri = data.data!!
                val imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false)

                val addPhotoBtn = binding.addPhotoLn
                val notFoundImg = binding.notFoundBooks
                val drawable = BitmapDrawable(resources, scaledBitmap)
                addPhotoBtn.background = drawable
                notFoundImg.visibility = View.GONE

                val photo = Photo()
                photo.image = imageBitmap
                if (dataPhoto.size == 1) dataPhoto[0] = photo
                else dataPhoto.add(photo)
            }

            else -> {}
        }
    }

    fun build(ctx: Context, width: Int? = null, func: ManageShopAddBookToShopDialog.() -> Unit): ManageShopAddBookToShopDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ManageShopAddBookToShopDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ManageShopAddBookToShopDialog.() -> Unit): ManageShopAddBookToShopDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}