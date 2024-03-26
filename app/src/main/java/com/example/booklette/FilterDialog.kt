package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.example.booklette.databinding.FilterDialogBinding
import com.maxkeppeler.sheets.core.Sheet
import java.text.NumberFormat
import java.util.Currency

private typealias PositiveListener = () -> Unit
class FilterDialog: Sheet() {
    override val dialogTag = "FilterSheet"
    private lateinit var binding: FilterDialogBinding

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
        return FilterDialogBinding.inflate(LayoutInflater.from(activity)).also { binding = it }.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rangeSlider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("USD")
            format.format(value.toDouble())
        }

        binding.rangeSlider.addOnChangeListener { slider, value, fromUser ->
            val values = slider.values
            val min = values[0].toInt()
            val max = values[1].toInt()
            binding.moneyRange.text = "${min}-${max}$"
        }

        val categories =  ArrayList<String>()
        categories.add("All")
        categories.add("Non-fiction")
        categories.add("Drama")
        categories.add("Comic")
        categories.add("Crime")
        categories.add("Horror")
        categories.add("Fantasy")
        categories.add("Young Adult")

        binding.filterCategoryDialogGV.adapter =
            activity?.let {FilterCategoriesGVAdapter(it, categories)}

        val type =  ArrayList<String>()
        type.add("Paper Back")
        type.add("Hard Cover")

        binding.filterTypeDialogGV.adapter =
            activity?.let {FilterTypeGVAdapter(it, type)}

        val ages =  ArrayList<String>()
        ages.add("All")
        ages.add("Lower 13")
        ages.add("13-18")
        ages.add("18+")
        binding.filterAgeDialogGV.adapter =
            activity?.let {FilterAgeGVAdapter(it, ages)}


        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
         //Hide the toolbar of the sheet, the title and the icon
    }

    fun build(ctx: Context, width: Int? = null, func: FilterDialog.() -> Unit): FilterDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [FilterDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: FilterDialog.() -> Unit): FilterDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}