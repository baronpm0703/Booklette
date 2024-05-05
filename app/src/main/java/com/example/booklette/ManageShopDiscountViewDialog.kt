package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.AddBookToShopDialogBinding
import com.example.booklette.databinding.ManageshopDiscountviewDialogBinding
import com.example.booklette.model.Photo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle
import java.text.SimpleDateFormat


class ManageShopDiscountViewDialog(discountDetail: HashMap<String, Comparable<*>?>): Sheet() {
    private lateinit var binding: ManageshopDiscountviewDialogBinding

    val discountDetail = discountDetail

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
        return ManageshopDiscountviewDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val format = SimpleDateFormat("yyyy-MM-dd")
        val startDate = format.format((discountDetail["startDate"] as Timestamp).toDate())
        val endDate = format.format((discountDetail["endDate"] as Timestamp).toDate())
        binding.nameET.setText(discountDetail["name"].toString())
        binding.startDateET.setText(startDate)
        binding.endDateET.setText(endDate)
        binding.percentET.setText(discountDetail["percent"].toString())
        binding.orderLimitET.setText(discountDetail["orderLimit"].toString())
        binding.introductionET.setText(discountDetail["introduction"].toString())
        binding.viewBookBtn.setOnClickListener {
            if (context is homeActivity) {
                var bdFragment = BookDetailFragment()

                var bundle = Bundle()
                bundle.putString("bookID", discountDetail["bookID"].toString())

                bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

                (context as homeActivity).changeFragmentContainer(
                    bdFragment,
                    (context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
                )

                positiveListener?.invoke()
            }
        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    fun build(ctx: Context, width: Int? = null, func: ManageShopDiscountViewDialog.() -> Unit): ManageShopDiscountViewDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ManageShopDiscountViewDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ManageShopDiscountViewDialog.() -> Unit): ManageShopDiscountViewDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}