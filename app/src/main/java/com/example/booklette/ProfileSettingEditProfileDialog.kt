package com.example.booklette

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.AddBookToShopDialogBinding
import com.example.booklette.databinding.ProfilesettingChangepasswordDialogBinding
import com.example.booklette.databinding.ProfilesettingEditprofileDialogBinding
import com.example.booklette.model.Photo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.Image
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso


class ProfileSettingEditProfileDialog: Sheet() {
    var avt: Bitmap? = null
    var name = ""
    var dob = ""
    var phone = ""
    var email = ""
    var address = ""

    private lateinit var binding: ProfilesettingEditprofileDialogBinding

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
        return ProfilesettingEditprofileDialogBinding.inflate(LayoutInflater.from(activity))
            .also { binding = it }.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        val auth = Firebase.auth
        db.collection("accounts").whereEqualTo("UID", auth.uid).get()
            .addOnSuccessListener { documents ->
                if (documents.size() != 1) return@addOnSuccessListener	// Failsafe

                for (document in documents) {
                    val avt = document.getString("avt")
                    val defaultAvt = "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Accounts%2Fdefault.png?alt=media&token=fd80f83e-7717-4279-a090-4dc97fa435b9"
                    val name = document.getString("fullname")
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    val dob = format.format((document.get("dob") as Timestamp).toDate())
                    val phone = document.getString("phone")
                    val email = auth.currentUser?.email
                    val address = document.getString("address")

                    if (!avt.isNullOrEmpty())
                        Picasso.get().load(avt).into(binding.avtIV)
                    else
                        Picasso.get().load(defaultAvt).into(binding.avtIV)
                    binding.nameETD.setText(name)
                    binding.dobET.setText(dob)
                    binding.phoneET.setText(phone)
                    binding.emailET.setText(email)
                    binding.addressETD.setText(address)
                }
            }


        val dobCalendar = Calendar.getInstance()
        val dobDate = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            dobCalendar.set(Calendar.YEAR, year)
            dobCalendar.set(Calendar.MONTH, month)
            dobCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = "yyyy-MM-dd"
            val dateFormat = SimpleDateFormat(format, java.util.Locale.US)
            binding.dobET.setText(dateFormat.format(dobCalendar.time))
        }
        binding.dobET.setOnClickListener {
            this.context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    dobDate,
                    dobCalendar.get(Calendar.YEAR),
                    dobCalendar.get(Calendar.MONTH),
                    dobCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        // Click out editext
        binding.changePasswordDialog.setOnClickListener {
            binding.nameETD.clearFocus()
            binding.dobET.clearFocus()
            binding.phoneET.clearFocus()
            binding.emailET.clearFocus()
            binding.addressETD.clearFocus()
        }
        binding.nameETD.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                name = binding.nameETD.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.nameETD.windowToken, 0)
            }
        }
        binding.dobET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                dob = binding.dobET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.dobET.windowToken, 0)
            }
        }
        binding.phoneET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                phone = binding.phoneET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.phoneET.windowToken, 0)
            }
        }
        binding.emailET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                email = binding.emailET.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.emailET.windowToken, 0)
            }
        }
        binding.addressETD.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // If EditText loses focus
            if (!hasFocus) {
                address = binding.addressETD.text.toString()
                // Hide the keyboard if it's currently showing
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.addressETD.windowToken, 0)
            }
        }

        //Apply
        binding.saveChangesBtn.setOnClickListener {
            name = binding.nameETD.text.toString()
            dob = binding.dobET.text.toString()
            phone = binding.phoneET.text.toString()
            email = binding.emailET.text.toString()
            address = binding.addressETD.text.toString()

            positiveListener?.invoke()
        }

        editAvtDialog(view)

        this.displayButtonsView(false)
//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
//        Hide the toolbar of the sheet, the title and the icon
    }

    private fun editAvtDialog(view: View) {
        val editAvtBtn = view.findViewById<CardView>(R.id.avtCV)
        val editAvtDialog = ProfileSettingAvtMethodDialog()
        editAvtBtn.setOnClickListener {
            activity?.let {
                editAvtDialog.show(it) {
                    style(SheetStyle.BOTTOM_SHEET)
                    onPositive {
                        val imageBitmap = editAvtDialog.getImage()
                        avt = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false)

                        view.findViewById<ImageView>(R.id.avtIV).setImageBitmap(avt)
                    }
                }
            }
        }
    }

    fun build(ctx: Context, width: Int? = null, func: ProfileSettingEditProfileDialog.() -> Unit): ProfileSettingEditProfileDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [ProfileSettingEditProfileDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: ProfileSettingEditProfileDialog.() -> Unit): ProfileSettingEditProfileDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }

}