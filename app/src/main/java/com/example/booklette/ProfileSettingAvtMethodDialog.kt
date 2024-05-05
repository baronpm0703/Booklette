package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.example.booklette.databinding.ProfilesettingAvtmethodDialogBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet


class ProfileSettingAvtMethodDialog: Sheet() {
    private lateinit var binding: ProfilesettingAvtmethodDialogBinding

    private lateinit var imageBitmap: Bitmap

    fun getImage(): Bitmap {
        return imageBitmap
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
        return ProfilesettingAvtmethodDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore

        binding.takePhotoBtn.setOnClickListener {
            val photoIntent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(photoIntent, 1111)
        }

        binding.choosefromGalleryBtn.setOnClickListener {
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
            1111 ->  if (null != data) {
                imageBitmap = data.extras?.get("data") as Bitmap

                positiveListener?.invoke()
            }
            2222 -> if (null != data) {
                val imageUri = data.data!!
                imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

                positiveListener?.invoke()
            }

            else -> {}
        }
    }

    fun build(ctx: Context, width: Int? = null, func: ProfileSettingAvtMethodDialog.() -> Unit): ProfileSettingAvtMethodDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ProfileSettingAvtMethodDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ProfileSettingAvtMethodDialog.() -> Unit): ProfileSettingAvtMethodDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}