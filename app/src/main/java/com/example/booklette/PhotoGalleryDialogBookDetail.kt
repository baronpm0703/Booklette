package com.example.booklette

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.booklette.databinding.ReviewDialogBookDetailBinding
import com.example.booklette.databinding.ReviewPhotoGalleryBookDetailBinding
import com.example.booklette.model.Photo
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale


class PhotoGalleryDialogBookDetail(
    private var initValue: InitFilterValuesReviewBookDetail
): Sheet() {
    override val dialogTag = "GallerySheet"

    private lateinit var binding: ReviewPhotoGalleryBookDetailBinding
    private lateinit var dataPhoto : ArrayList<Photo>
    private var adapter: reviewBookDetailPhotoGalleryGridAdapter? = null
    private val IMAGE_REQUEST_CODE = 1

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
        return ReviewPhotoGalleryBookDetailBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataPhoto = ArrayList()
        if (
            ContextCompat.checkSelfPermission(
                requireActivity().applicationContext, Manifest.permission.READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            ActivityCompat.requestPermissions(requireActivity(), permissions, IMAGE_REQUEST_CODE);
        } else {
            Toast.makeText(requireActivity(), "Permission Granted", Toast.LENGTH_SHORT).show()
            dataPhoto = loadImagesFromSDCard()
            adapter?.notifyDataSetChanged()
        }
        adapter = reviewBookDetailPhotoGalleryGridAdapter(requireActivity(), dataPhoto)
        binding.gallery.adapter = adapter
        binding.gallery.setOnItemClickListener { adapterView, view, i, l ->

            Toast.makeText(requireActivity(), " Selected Photo is "+ dataPhoto.get(i).nameFile ,
                Toast.LENGTH_SHORT).show()

        }

        //Apply
        binding.agreeBtn.setOnClickListener {
            positiveListener?.invoke()
        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IMAGE_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, load images
            dataPhoto = loadImagesFromSDCard()
            adapter?.notifyDataSetChanged()

        } else {
            // Permission denied, handle accordingly
            Toast.makeText(requireActivity(), "Permission Denied", Toast.LENGTH_LONG).show()
            // You may want to display a message or request permission again
        }
    }

    private fun loadImage(file: File): Photo {
        var photo = Photo()
        photo.id = Photo.totalID
        photo.nameFile = file.path
        photo.id?.let { Photo.updateTotalID(it) }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        photo.image = bitmap

        return photo
    }

    private fun loadImagesFromSDCard() : ArrayList<Photo> {
        var listPhoto = ArrayList<Photo>()
        // Check if external storage is available
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val sdCard = Environment.getExternalStorageDirectory()
            val imageDir = File(sdCard.absolutePath + "/Pictures") // Change this to your desired directory

            // List all files in the directory
            val imageFiles = imageDir.listFiles { _, name -> name.endsWith(".jpg") || name.endsWith(".png") }

            // Load each image file
            imageFiles?.forEach { file ->
                listPhoto.add(loadImage(file))
            }
        }

        return listPhoto
    }

    fun build(ctx: Context, width: Int? = null, func: PhotoGalleryDialogBookDetail.() -> Unit): PhotoGalleryDialogBookDetail {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [PhotoGalleryDialogBookDetail] directly. */
    fun show(ctx: Context, width: Int? = null, func: PhotoGalleryDialogBookDetail.() -> Unit): PhotoGalleryDialogBookDetail {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}