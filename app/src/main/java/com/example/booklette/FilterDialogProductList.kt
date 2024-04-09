package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.maxkeppeler.sheets.core.Sheet
import java.text.NumberFormat
import java.util.Currency
import com.example.booklette.databinding.FilterDialogProductListBinding

private typealias PositiveListener = () -> Unit
class FilterDialogProductList(
    private val needRemoveCategory: Boolean,
    private var initValue: InitFilterValuesProductList
): Sheet() {
    override val dialogTag = "FilterSheet"
    private var moneyRange = 0.0F
    private var selectedSlider = arrayListOf("", "")
    private lateinit var binding: FilterDialogProductListBinding

    private var chosenType = arrayListOf<String>()
    private var chosenCategory = arrayListOf<String>()
    private var chosenAge = arrayListOf<String>()

    private var categories: ArrayList<String> = ArrayList()
    private var type: ArrayList<String> = ArrayList()
    private var ages: ArrayList<String> = ArrayList()

    // Boolean Array
    private var chosenTypeTrueTableArray: BooleanArray
    private var chosenCategoryTrueTableArray: BooleanArray
    private var chosenAgeTrueTableArray: BooleanArray

    // Adapter
    private var filterTypeDialogGVAdapter: FilterTypeGVAdapter? = null
    private var filterCategoriesGVAdapter: FilterCategoriesGVAdapter? = null
    private var filterAgeGVAdapter: FilterAgeGVAdapter? = null

    init {
        type.add("Paper Back")
        type.add("Hard Cover")
        chosenTypeTrueTableArray = BooleanArray(type.size)

        categories.add("All")
        categories.add("Non-fiction")
        categories.add("Drama")
        categories.add("Comic")
        categories.add("Crime")
        categories.add("Horror")
        categories.add("Fantasy")
        categories.add("Young Adult")
        chosenCategoryTrueTableArray = BooleanArray(categories.size)

        ages.add("All")
        ages.add("Lower 13")
        ages.add("13-18")
        ages.add("18+")
        chosenAgeTrueTableArray = BooleanArray(ages.size)
    }

    fun getSliderValues(): ArrayList<String> {
        return selectedSlider
    }

    fun getChosenType(): ArrayList<String> {
        return chosenType
    }
    fun getChosenAge(): ArrayList<String> {
        return chosenAge
    }

    fun getChosenCategory(): ArrayList<String> {
        return chosenCategory
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

    fun updateInitValues(initValues: InitFilterValuesProductList) {
        this.initValue = initValues
        moneyRange = (initValue.rangslider[1] - initValue.rangslider[0]) / 1000
    }

    override fun onCreateLayoutView(): View {
        return FilterDialogProductListBinding.inflate(LayoutInflater.from(activity)).also { binding = it }.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rangeSlider.values = arrayListOf(0.0F, 100.0F)
        binding.rangeSlider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("VND")
            val value_to_display = (value * moneyRange / 100 + initValue.rangslider[0] / 1000)
            format.format(value_to_display.toDouble())
        }

        binding.rangeSlider.addOnChangeListener { slider, value, fromUser ->
            val values = slider.values
            val min = (values[0] * moneyRange / 100 + initValue.rangslider[0] / 1000).toInt()
            val max = (values[1] * moneyRange / 100 + initValue.rangslider[0] / 1000).toInt()
            selectedSlider[0] = min.toString() // Been divided for 1000
            selectedSlider[1] = max.toString()
            binding.moneyRange.text = "${min}.000-${max}.000 VND"
        }

        // Category
        if (needRemoveCategory) {
            binding.filterCategoryDialog.visibility = View.GONE
            binding.filterCategoryDialogGV.visibility = View.GONE

        } else {
            applyFilterCategoriesAdapter()
        }

        // Type
        applyFilterTypesAdapter()
        //Age
        applyFilterAgesAdapter()

        //Apply
        binding.applyFilterBtn.setOnClickListener {
            positiveListener?.invoke()
        }


        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    fun applyFilterCategoriesAdapter() {

        if (filterCategoriesGVAdapter == null) {
            filterCategoriesGVAdapter = FilterCategoriesGVAdapter(requireActivity(), categories)
            filterCategoriesGVAdapter!!.setCheckedItems(chosenCategoryTrueTableArray)
        } else {
            // Retrieve the checked items from the adapter and update the UI
            filterCategoriesGVAdapter!!.setCheckedItems(chosenCategoryTrueTableArray)
        }

        binding.filterCategoryDialogGV.adapter = filterCategoriesGVAdapter
        // Category OnIemClick
        binding.filterCategoryDialogGV.setOnItemClickListener { parent, view, position, id ->
            if (!chosenCategory.contains(categories[position])) {
                chosenCategory.add(categories[position])
                chosenCategoryTrueTableArray[position] = true
            } else {
                chosenCategory.remove(chosenCategory.filter { ty -> ty == categories[position] }.single())
                chosenCategoryTrueTableArray[position] = false
            }

            filterCategoriesGVAdapter?.setCheckedItems(chosenCategoryTrueTableArray)
        }
    }
    fun applyFilterTypesAdapter() {
        if (filterTypeDialogGVAdapter == null) {
            filterTypeDialogGVAdapter = FilterTypeGVAdapter(requireActivity(), type)
            filterTypeDialogGVAdapter!!.setCheckedItems(chosenTypeTrueTableArray)
        } else {
            // Retrieve the checked items from the adapter and update the UI
            filterTypeDialogGVAdapter!!.setCheckedItems(chosenTypeTrueTableArray)
        }

        binding.filterTypeDialogGV.adapter = filterTypeDialogGVAdapter
        // Type OnIemClick
        binding.filterTypeDialogGV.setOnItemClickListener { parent, view, position, id ->
            if (!chosenType.contains(type[position])) {
                chosenType.add(type[position])
                chosenTypeTrueTableArray[position] = true
            } else {
                chosenType.remove(chosenType.single { ty -> ty == type[position] })
                chosenTypeTrueTableArray[position] = false
            }

            filterTypeDialogGVAdapter?.setCheckedItems(chosenTypeTrueTableArray)
        }
    }
    fun applyFilterAgesAdapter() {
        if (filterAgeGVAdapter == null) {
            filterAgeGVAdapter = FilterAgeGVAdapter(requireActivity(), ages)
            filterAgeGVAdapter!!.setCheckedItems(chosenAgeTrueTableArray)
        } else {
            // Retrieve the checked items from the adapter and update the UI
            filterAgeGVAdapter!!.setCheckedItems(chosenAgeTrueTableArray)
        }

        binding.filterAgeDialogGV.adapter = filterAgeGVAdapter
        // Ages OnIemClick
        binding.filterAgeDialogGV.setOnItemClickListener { parent, view, position, id ->
            if (!chosenAge.contains(ages[position])) {
                chosenAge.add(ages[position])
                chosenAgeTrueTableArray[position] = true
            } else {
                chosenAge.remove(chosenAge.single { ty -> ty == ages[position] })
                chosenAgeTrueTableArray[position] = false
            }

            filterAgeGVAdapter?.setCheckedItems(chosenAgeTrueTableArray)
        }
    }

    fun build(ctx: Context, width: Int? = null, func: FilterDialogProductList.() -> Unit): FilterDialogProductList {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [FilterDialogProductList] directly. */
    fun show(ctx: Context, width: Int? = null, func: FilterDialogProductList.() -> Unit): FilterDialogProductList {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}