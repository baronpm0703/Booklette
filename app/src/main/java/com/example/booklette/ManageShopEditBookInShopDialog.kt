package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.EditBookInShopDialogBinding
import com.example.booklette.model.Photo
import com.google.firebase.Timestamp
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso
import java.net.URL


class ManageShopEditBookInShopDialog(
    private var initValue: ManageShopAddBookToShopDialogInitFilterValues
): Sheet() {
    override val dialogTag = "AddBook"

    private lateinit var binding: EditBookInShopDialogBinding
    private var bookName: String = ""
    private var bookAuthor: String = ""
    private var bookCategory: String = ""
    private var bookType: String = ""
    private var bookDesc: String = ""
    private var bookPrice: String = ""
    private var bookQuantity: String = ""
    private lateinit var dataPhoto : ArrayList<Photo>
    private lateinit var photoGalleryDialogBookDetail: PhotoGalleryDialogBookDetail
    private lateinit var chosenPhotoAdapter: ChosenReviewPhotoBookDetailRVAdapter

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
            "releaseDate" to Timestamp.now(),
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
        return EditBookInShopDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init data Photo
        dataPhoto = ArrayList()
        chosenPhotoAdapter = activity?.let { ChosenReviewPhotoBookDetailRVAdapter(it, dataPhoto) }!!
//        binding.chosenPhoto.adapter = chosenPhotoAdapter

        bookName = initValue.name
        binding.bookNameET.setText(bookName)
        bookAuthor = initValue.author
        binding.bookAuthorET.setText(bookAuthor)
        bookCategory = initValue.category
        binding.bookCategoryET.setText(bookCategory)
        bookDesc = initValue.desc
        binding.bookDescET.setText(bookDesc)
        bookPrice = initValue.price
        binding.bookPriceET.setText(bookPrice)
        bookQuantity = initValue.quantity
        binding.bookQuantityET.setText(bookQuantity)
        // Read image
        val thread = Thread {
            run {
                val conn = URL(initValue.image).openConnection()
                conn.doInput = true
                conn.connect()
                val input = conn.getInputStream()
                val img = Photo()
                img.image = BitmapFactory.decodeStream(input)
                dataPhoto.add(img)
            }
        }
        thread.start()
        thread.join()
        // Get type for Spinner
        bookType = initValue.type
        val spinner = binding.bookTypeSpin
        val items = resources.getStringArray(R.array.manageshop_mybooks_add_type)
        spinner.setSelection(items.indexOf(bookType))

        if (spinner != null) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        // Click out editext
        binding.addBookDialog.setOnClickListener {
            binding.bookNameET.clearFocus()
            binding.bookAuthorET.clearFocus()
            binding.bookCategoryET.clearFocus()
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
        binding.bookCategoryET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                bookCategory = binding.bookCategoryET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.bookCategoryET.windowToken, 0)
            }
        }
//        binding.bookTypeET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
//            // If EditText loses focus
//            if (!hasFocus) {
//                bookType = binding.bookTypeET.text.toString()
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
            bookCategory = binding.bookCategoryET.text.toString()
            bookType = binding.bookTypeSpin.selectedItem.toString()
            bookDesc = binding.bookDescET.text.toString()
            bookPrice = binding.bookPriceET.text.toString()
            bookQuantity = binding.bookQuantityET.text.toString()
            positiveListener?.invoke()
        }

        //Set Up horizontal layout
        val h_linerLayout = activity?.let {LinearLayoutManager(it)}
        if (h_linerLayout != null) {
            h_linerLayout.orientation = LinearLayoutManager.HORIZONTAL
        }
        binding.chosenPhoto.layoutManager = h_linerLayout

        val itemDecoration: RecyclerView.ItemDecoration = DividerItemDecoration(
            activity,
            DividerItemDecoration.HORIZONTAL)
        binding.chosenPhoto.addItemDecoration(itemDecoration)

        val initValues = InitFilterValuesReviewBookDetail()
        photoGalleryDialogBookDetail = PhotoGalleryDialogBookDetail(initValues)
        binding.notFoundBooks.setOnClickListener {
            activity?.let {
                photoGalleryDialogBookDetail.show(it){
                    style(SheetStyle.DIALOG)
                    onPositive {
                        this.dismiss()
                        updateChosenPhoto(getChosenPhoto())
                    }
                }
            }

        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateChosenPhoto(dataPhoto: ArrayList<Photo>){
        this.dataPhoto = dataPhoto
        chosenPhotoAdapter.updateDataPhoto(this.dataPhoto)
        chosenPhotoAdapter.notifyDataSetChanged()
        binding.chosenPhoto.visibility = View.VISIBLE
    }

    fun build(ctx: Context, width: Int? = null, func: ManageShopEditBookInShopDialog.() -> Unit): ManageShopEditBookInShopDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ManageShopEditBookInShopDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ManageShopEditBookInShopDialog.() -> Unit): ManageShopEditBookInShopDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}