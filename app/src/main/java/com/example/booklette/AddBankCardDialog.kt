package com.example.booklette

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.example.booklette.databinding.AddBankCardBinding
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet

class AddBankCardDialog: Sheet() {
    override val dialogTag = "Add Bank Card"
    private lateinit var binding: AddBankCardBinding

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
        return AddBankCardBinding.inflate(LayoutInflater.from(activity)).also { binding = it }.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
         //Hide the toolbar of the sheet, the title and the icon
    }

    fun build(ctx: Context, width: Int? = null, func: AddBankCardDialog.() -> Unit): AddBankCardDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [AddBankCardDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: AddBankCardDialog.() -> Unit): AddBankCardDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }
}