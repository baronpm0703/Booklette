package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.example.booklette.databinding.ReviewDialogBookDetailBinding
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import java.text.NumberFormat
import java.util.Currency

class ReviewDialogBookDetail(
    private var initValue: InitFilterValuesReviewBookDetail
): Sheet() {
    override val dialogTag = "ReviewSheet"

    private lateinit var binding: ReviewDialogBookDetailBinding
    private var client_review: String = ""


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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ratingStarBar.rating = initValue.rating

        if (!initValue.text.equals("Your Review")) {
            binding.reviewText.setText(initValue.text)
            binding.reviewText.setTextColor(Color.parseColor("#000000"))
        }


        binding.reviewText.addTextChangedListener {object: TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.reviewText.setTextColor(Color.parseColor("#000000"))
                }

                override fun afterTextChanged(s: Editable?) {
                    Toast.makeText(activity, "Changed", Toast.LENGTH_SHORT).show()
                    client_review = s.toString()
                }

            }
        }

        //Apply
        binding.sendReviewBtn.setOnClickListener {
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