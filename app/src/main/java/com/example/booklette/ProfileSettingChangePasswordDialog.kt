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
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.AddBookToShopDialogBinding
import com.example.booklette.databinding.ProfilesettingChangepasswordDialogBinding
import com.example.booklette.model.Photo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle


class ProfileSettingChangePasswordDialog: Sheet() {
    var oldPassword = ""
    var newPassword = ""
    var confimPassword = ""

    private lateinit var binding: ProfilesettingChangepasswordDialogBinding

    fun getOldPwd(): String {
        return oldPassword
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
        return ProfilesettingChangepasswordDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore

        // Click out editext
        binding.changePasswordDialog.setOnClickListener {
            binding.oldPwdET.clearFocus()
            binding.newPwdET.clearFocus()
            binding.confirmPwdET.clearFocus()
        }
        binding.oldPwdET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                oldPassword = binding.oldPwdET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.oldPwdET.windowToken, 0)
            }
        }
        binding.newPwdET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                newPassword = binding.newPwdET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.newPwdET.windowToken, 0)
            }
        }
        binding.confirmPwdET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                confimPassword = binding.confirmPwdET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.confirmPwdET.windowToken, 0)
            }
        }

        //Apply
        binding.savePwdBtn.setOnClickListener {
            oldPassword = binding.oldPwdET.text.toString()
            newPassword = binding.newPwdET.text.toString()
            confimPassword = binding.confirmPwdET.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confimPassword.isEmpty()) {
                Toast.makeText(context, resources.getString(R.string.profilesetting_password_change_empty), Toast.LENGTH_SHORT).show()
            }
            else if (newPassword != confimPassword) {
                Toast.makeText(context, resources.getString(R.string.profilesetting_password_change_nomatch), Toast.LENGTH_SHORT).show()
            }
            else
                positiveListener?.invoke()
        }

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    fun build(ctx: Context, width: Int? = null, func: ProfileSettingChangePasswordDialog.() -> Unit): ProfileSettingChangePasswordDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ProfileSettingChangePasswordDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ProfileSettingChangePasswordDialog.() -> Unit): ProfileSettingChangePasswordDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}