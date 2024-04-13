package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getSystemService
import com.example.booklette.databinding.ReviewDialogBookDetailBinding
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import java.util.Locale


class ReviewDialogBookDetail(
    private var initValue: InitFilterValuesReviewBookDetail
): Sheet() {
    override val dialogTag = "ReviewSheet"

    private lateinit var binding: ReviewDialogBookDetailBinding
    private var client_review: String = ""
    private var client_rating: Float = 0.0F

    fun getClientReview(): String {
        return client_review
    }
    fun getClientRating(): Float {
        return client_rating
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


        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
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