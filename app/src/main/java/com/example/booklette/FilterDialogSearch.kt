package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import java.text.NumberFormat
import java.util.Currency
import com.example.booklette.databinding.FilterDialogSearchBinding

class FilterDialogSearch: Sheet() {
    override val dialogTag = "FilterSheet"
    private lateinit var binding: FilterDialogSearchBinding
    private var chosenType = arrayListOf<String>()
    private val type = arrayListOf("Paper Back", "Hard Cover")
    private var chosenTypeTrueTableArray = BooleanArray(type.size)
    private var filterTypeDialogGVAdapter: FilterTypeGVAdapter? = null

    init {
        chosenTypeTrueTableArray.fill(false)
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
        return FilterDialogSearchBinding.inflate(LayoutInflater.from(activity)).also { binding = it }.root
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

        // Initialize adapter only if null, otherwise, reuse existing adapter
        if (filterTypeDialogGVAdapter == null) {
            filterTypeDialogGVAdapter = FilterTypeGVAdapter(requireActivity(), type)
            filterTypeDialogGVAdapter!!.setCheckedItems(chosenTypeTrueTableArray)
        } else {
            // Retrieve the checked items from the adapter and update the UI
            filterTypeDialogGVAdapter!!.setCheckedItems(chosenTypeTrueTableArray)
        }

        binding.filterTypeDialogGV.adapter = filterTypeDialogGVAdapter

        binding.filterTypeDialogGV.setOnItemClickListener { adapterView, view, index, l ->
            if (!chosenType.contains(type[index])) {
                chosenType.add(type[index])
                chosenTypeTrueTableArray[index] = true
//                Log.d("OnItemCLick", chosenTypeTrueTableArray[index].toString())
            } else {
                chosenType.remove(chosenType.filter { ty -> ty == type[index] }.single())
                chosenTypeTrueTableArray[index] = false
            }
            // Update the adapter with the new checked state
            (filterTypeDialogGVAdapter)?.setCheckedItems(chosenTypeTrueTableArray)
        }




        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
         //Hide the toolbar of the sheet, the title and the icon
    }


    fun build(ctx: Context, width: Int? = null, func: FilterDialogSearch.() -> Unit): FilterDialogSearch {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [FilterDialogSearch] directly. */
    fun show(ctx: Context, width: Int? = null, func: FilterDialogSearch.() -> Unit): FilterDialogSearch {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}