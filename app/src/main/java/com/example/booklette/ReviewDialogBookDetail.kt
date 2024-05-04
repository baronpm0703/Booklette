package com.example.booklette

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.ReviewDialogBookDetailBinding
import com.example.booklette.model.Photo
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle
import java.io.IOException


class ReviewDialogBookDetail(
    private var initValue: InitFilterValuesReviewBookDetail
): Sheet() {
    override val dialogTag = "ReviewSheet"

    private lateinit var binding: ReviewDialogBookDetailBinding
    private var client_review: String = ""
    private var client_rating: Float = 0.0F
    private lateinit var dataPhoto : ArrayList<Photo>
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

    fun updateinitValue(initValue: InitFilterValuesReviewBookDetail){
        this.initValue = initValue
    }

    override fun onCreateLayoutView(): View {
        return ReviewDialogBookDetailBinding.inflate(LayoutInflater.from(activity))
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
        if (initValue.reviewPhotos.isNotEmpty())
        {
            dataPhoto = initValue.reviewPhotos
            binding.chosenPhoto.visibility = View.VISIBLE
        }
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

        binding.notFoundBooks.setOnClickListener {
            openGallery()
        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    @SuppressLint("Range")
    fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var displayName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return displayName
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryLauncher.launch(intent)
    }
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = activity?.contentResolver?.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle selected photos
            val selectedPhotosUriList = result.data?.clipData?.let { clipData ->
                (0 until clipData.itemCount).map { index ->
                    clipData.getItemAt(index).uri
                }
            } ?: listOf(result.data?.data)

            // Do something with selectedPhotosUriList
            dataPhoto.clear()

            for (uri in selectedPhotosUriList) {
                val item = uri?.let { uriToBitmap(it) }
                if (item != null)  {
                    var photo = Photo()
                    photo.id = Photo.totalID
                    photo.nameFile = activity?.let { getFileName(it.contentResolver, uri) }
                    photo.id?.let { Photo.updateTotalID(it) }
                    val bitmap = item
                    photo.image = bitmap
                    dataPhoto.add(photo)
                }
            }

            updateChosenPhoto(dataPhoto)
            // For example, display selected images in ImageView
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    fun updateChosenPhoto(dataPhoto: ArrayList<Photo>){
        this.dataPhoto = dataPhoto
        chosenPhotoAdapter.updateDataPhoto(this.dataPhoto)
        chosenPhotoAdapter.notifyDataSetChanged()
        binding.chosenPhoto.visibility = View.VISIBLE
    }
    fun build(ctx: Context, width: Int? = null, func: ReviewDialogBookDetail.() -> Unit): ReviewDialogBookDetail {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ReviewDialogBookDetail] directly. */
    fun show(ctx: Context, width: Int? = null, func: ReviewDialogBookDetail.() -> Unit): ReviewDialogBookDetail {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}