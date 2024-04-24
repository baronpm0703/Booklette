package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.AddBookToShopDialogBinding
import com.example.booklette.model.ManageShopNewBookObject
import com.example.booklette.model.Photo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle


class ManageShopAddBookToShopDialog(
    private var initValue: ManageShopAddBookToShopDialogInitFilterValues
): Sheet() {
    override val dialogTag = "AddBook"

    private lateinit var binding: AddBookToShopDialogBinding
    private var client_review: String = ""
    private var client_rating: Float = 0.0F
    private lateinit var dataPhoto : ArrayList<Photo>
    private lateinit var photoGalleryDialogBookDetail: PhotoGalleryDialogBookDetail
    private lateinit var chosenPhotoAdapter: ChosenReviewPhotoBookDetailRVAdapter

    fun getClientReview(): String {
        return client_review
    }
    fun getClientRating(): Float {
        return client_rating
    }

    fun getImage(): ArrayList<Photo> {
        return dataPhoto
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

        // Rating bar
        client_rating = initValue.rating
        binding.ratingStarBar.rating = initValue.rating
        binding.ratingStarBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            client_rating = rating
        }

        if (initValue.text.isNotEmpty()){
            client_review = initValue.text
            binding.reviewText.setText(initValue.text)
        }
        // Click out editext
        binding.reviewDialog.setOnClickListener {
            binding.reviewText.clearFocus()
        }
        binding.reviewText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                client_review = binding.reviewText.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.reviewText.windowToken, 0)
            }
        }

        //Apply
        binding.sendReviewBtn.setOnClickListener {
            client_review = binding.reviewText.text.toString()
            positiveListener?.invoke()
        }


        // Init data Photo
        dataPhoto = ArrayList()
        chosenPhotoAdapter = activity?.let { ChosenReviewPhotoBookDetailRVAdapter(it, dataPhoto) }!!
        binding.chosenPhoto.adapter = chosenPhotoAdapter

        //Set Up horizontal layout
        val h_linerLayout = activity?.let {LinearLayoutManager(it)}
        if (h_linerLayout != null) {
            h_linerLayout.orientation = LinearLayoutManager.HORIZONTAL
        }
        binding.chosenPhoto.layoutManager = h_linerLayout

//        val itemDecoration: RecyclerView.ItemDecoration = DividerItemDecoration(
//            activity,
//            DividerItemDecoration.HORIZONTAL)
//        binding.chosenPhoto.addItemDecoration(itemDecoration)

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

    private fun addNewBook(view: View, newBook: ManageShopNewBookObject) {
        val auth = Firebase.auth
        val db = Firebase.firestore

        val bookColl = db.collection("books")
        bookColl.get().addOnSuccessListener {
            val documents = it.documents
            var max: Long = 0
            for (document in documents) {
                val id = document.get("bookID").toString().filter { it.isDigit() }.toLong()

                if (id > max) max = id
            }
            val newBookID = "BK" + (max + 1)
            val newBookMap = hashMapOf(
                "author" to newBook.author,
                "best-deal-sale" to 0.0,
                "bookID" to newBookID,
                "description" to newBook.description,
                "genre" to newBook.genre,
                "image" to newBook.image,
                "name" to newBook.name,
                "releaseDate" to newBook.releaseDate,
                "review" to emptyArray<Any>(),
                "top-book" to newBook.topBook,
                "type" to newBook.type
            )

            bookColl.add(newBookMap)

            db.collection("accounts").whereEqualTo("UID", auth.uid).get()
                .addOnSuccessListener { documents ->
                    if (documents.size() != 1) return@addOnSuccessListener	// Failsafe

                    val document = documents.documents[0]
                    val store = document.getDocumentReference("store")
                    store!!.get().addOnSuccessListener { storeSnapshot ->
                        val bookList = storeSnapshot.get("items") as HashMap<String, Map<String, Any>>

                        val newStoreBookMap = hashMapOf(
                            "discount" to "",
                            "price" to newBook.price,
                            "remain" to newBook.quantity,
                            "sold" to newBook.quantity,
                            "status" to ""
                        )

                        bookList[newBookID] = newStoreBookMap

                        store.update("items", bookList)
                    }
                }

            Handler().postDelayed({

            }, 2000)
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